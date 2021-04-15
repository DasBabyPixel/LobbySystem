package eu.darkcube.system.lobbysystem.pserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.database.Database;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelectionWoolBattle;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.GsonSerializer;
import eu.darkcube.system.lobbysystem.util.GsonSerializer.DontSerialize;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServer.State;
import eu.darkcube.system.pserver.common.PServerProvider;
import eu.darkcube.system.pserver.common.UniqueId;
import eu.darkcube.system.pserver.common.packet.PServerSerializable;

public class PServerDataManager {

	private static final Database database = CloudNetDriver.getInstance()
			.getDatabaseProvider()
			.getDatabase("pserver_userslots");

	public static ItemBuilder getDisplayItem(User user, PServerUserSlot slot) {
		if (slot.isUsed()) {
			JsonObject data = slot.getData();
			if (data.has("task")) {
				ServiceTask task = CloudNetDriver.getInstance()
						.getServiceTaskProvider()
						.getServiceTask(data.get("task").getAsString());
				return getDisplayItemGamemode(user, task);
			}
		}
		return null;
	}

	public static ItemBuilder getDisplayItemGamemode(User user, ServiceTask task) {
		if (Lobby.getInstance().getDataManager().getWoolBattleTasks().contains(task.getName())) {
			return new InventoryGameServerSelectionWoolBattle.Func().apply(user, task);
		}
		return null;
	}

	public static class PServerUserSlots {

		private User user;
		private Map<Integer, PServerUserSlot> slots = null;

		public PServerUserSlots(User user) {
			this.user = user;
			if (PServerSupport.isSupported()) {
				if (database.contains(user.getUniqueId().toString())) {
					slots = GsonSerializer.gson.fromJson(database.get(user.getUniqueId().toString()).toJson(),
							new TypeToken<Map<Integer, PServerUserSlot>>() {
							}.getType());
				} else {
					slots = new HashMap<>();
					database.insert(user.getUniqueId().toString(), createDocument());
				}
			}
			/*
			 * if (slots == null) { slots = new HashMap<>();
			 * database.insert(user.getUniqueId().toString(), new
			 * JsonDocument(GsonSerializer.gson.toJson(slots))); }
			 */
		}

		private JsonDocument createDocument() {
			return JsonDocument.newDocument(GsonSerializer.gson.toJson(slots));
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
			if (PServerSupport.isSupported()) {
				boolean changed = false;
				for (PServerUserSlot slot : slots.values()) {
					if (slot.changed) {
						changed = true;
						break;
					}
				}
				if (changed) {
					database.update(user.getUniqueId().toString(), createDocument());
				}
			}
		}
	}

	public static class PServerUserSlot {

		@DontSerialize
		private boolean changed = false;
		private UniqueId pserverId = null;
		private JsonObject data = null;

		public void load(UniqueId pserverId) {
			this.pserverId = pserverId;
			this.changed = true;
		}

		private JsonObject newData() {
			data = new JsonObject();
			data.addProperty("private", true);
			changed = true;
			return data;
		}

		public PServer startPServer() {
			checkConfig();
			PServer ps = getPServer();
			if (ps == null) {
				int online = 0;
				PServer.State state = State.OFFLINE;
				boolean privateServer = getData().get("private").getAsJsonPrimitive().getAsBoolean();
				boolean temporary = getData().has("task");
				long startedAt = System.currentTimeMillis();
				Collection<UUID> owners = new ArrayList<>();
				PServerSerializable ser = new PServerSerializable(pserverId, online, state, privateServer, temporary,
						startedAt, owners, null, PServerProvider.getInstance().newName());
				if (data.has("task")) {
					ps = PServerProvider.getInstance()
							.createPServer(ser,
									CloudNetDriver.getInstance()
											.getServiceTaskProvider()
											.getServiceTask(getData().get("task").getAsString()));
				} else {
					ps = PServerProvider.getInstance().createPServer(ser);
				}
				ps.start();
			}
			return ps;
		}

		public PServer getPServer() {
			checkConfig();
			return PServerProvider.getInstance().getPServer(pserverId);
		}

		private void checkConfig() {
			if (pserverId == null) {
				throw new InvalidConfigurationException();
			}
		}

		public JsonObject getData() {
			return data == null ? newData() : data;
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
