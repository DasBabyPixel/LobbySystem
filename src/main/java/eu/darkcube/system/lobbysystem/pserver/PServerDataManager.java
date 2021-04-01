package eu.darkcube.system.lobbysystem.pserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.juliarn.relocation.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.database.Database;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.GsonSerializer;
import eu.darkcube.system.lobbysystem.util.GsonSerializer.DontSerialize;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServer.State;
import eu.darkcube.system.pserver.common.PServerProvider;
import eu.darkcube.system.pserver.common.UniqueId;
import eu.darkcube.system.pserver.common.packet.PServerSerializable;

public class PServerDataManager {

	private static final Database database = CloudNetDriver.getInstance()
			.getDatabaseProvider()
			.getDatabase("pserver_userslots");

	public static class PServerUserSlots {

		private User user;
		private Map<Integer, PServerUserSlot> slots = null;

		public PServerUserSlots(User user) {
			this.user = user;
			if (database.contains(user.getUniqueId().toString())) {
				slots = GsonSerializer.gson.fromJson(database.get(user.getUniqueId().toString()).toJson(),
						new TypeToken<Map<Integer, PServerUserSlot>>() {
						}.getType());
			} else {
				slots = new HashMap<>();
				database.insert(user.getUniqueId().toString(),
						new JsonDocument((Object) GsonSerializer.gson.toJsonTree(slots)));
			}
			/*
			 * if (slots == null) { slots = new HashMap<>();
			 * database.insert(user.getUniqueId().toString(), new
			 * JsonDocument(GsonSerializer.gson.toJson(slots))); }
			 */
		}

		public User getUser() {
			return user;
		}

		public PServerUserSlot getSlot(int slot) {
			PServerUserSlot s = slots.get(slot);
			if (s == null) {
				s = new PServerUserSlot();
				slots.put(slot, s);
			}
			return s;
		}

		public void save() {
			boolean changed = false;
			for (PServerUserSlot slot : slots.values()) {
				if (slot.changed) {
					changed = true;
					break;
				}
			}
			if (changed) {
				database.update(user.getUniqueId().toString(),
						new JsonDocument((Object) GsonSerializer.gson.toJsonTree(slots)));
			}
		}
	}

	public static class PServerUserSlot {

		@DontSerialize
		private boolean changed = false;
		private UniqueId pserverId = null;
		private JsonObject data = null;

		public PServerUserSlot() {

		}

		public void load(UniqueId pserverId, JsonObject data) {
			this.pserverId = pserverId;
			this.data = data;
			this.changed = true;
		}

		public void unload() {
			load(null, null);
		}

		public PServer startPServer() {
			checkConfig();
			PServer ps = getPServer();
			if (ps == null) {
				int online = 0;
				PServer.State state = State.OFFLINE;
				boolean privateServer = false;
				boolean temporary = data.has("task");
				long startedAt = System.currentTimeMillis();
				Collection<UUID> owners = new ArrayList<>();
				PServerSerializable ser = new PServerSerializable(pserverId, online, state, privateServer, temporary,
						startedAt, owners, PServerProvider.getInstance().newName());

				if (data.has("task")) {
					ps = PServerProvider.getInstance()
							.createPServer(ser, new Gson().fromJson(data.get("task").toString(), ServiceTask.class));
				} else {
					ps = PServerProvider.getInstance().createPServer(ser);
				}
			}
			return ps;
		}

		public PServer getPServer() {
			checkConfig();
			return PServerProvider.getInstance().getPServer(pserverId);
		}

		private void checkConfig() {
			if (pserverId == null || data == null) {
				throw new InvalidConfigurationException();
			}
		}

		public JsonObject getData() {
			return data;
		}

		public UniqueId getPServerId() {
			return pserverId;
		}

		public boolean isUsed() {
			return pserverId != null;
		}

		public static class InvalidConfigurationException extends RuntimeException {
			private static final long serialVersionUID = 9158875635797975221L;
		}
	}
}
