package eu.darkcube.system.lobbysystem.inventory.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.ServiceInfoSnapshotUtil;
import eu.darkcube.system.GameState;
import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;

public abstract class MinigameInventoryOld extends Inventory {

	public MinigameInventoryOld(String displayName, InventoryType type) {
		super(Bukkit.createInventory(null, 6 * 9, displayName), type);
	}

	private static final int[] SLOTS = new int[] { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30,
			31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };

	private static final int[] SORT = new int[] { 04, 03, 02, 01, 02, 03, 04, 05, 04, 03, 02, 03, 04, 05, 06, 05, 04,
			03, 04, 05, 06, 07, 06, 05, 04, 05, 06, 07 };

	@Override
	public void playAnimation(User user) {
		animate(user, false);
	}

	@Override
	public void skipAnimation(User user) {
		animate(user, true);
	}

	protected abstract ItemStack getMinigameItem(User user);

	protected abstract Set<String> getCloudTasks();

	@SuppressWarnings("deprecation")
	private void calculateItems(Map<Integer, Map<Integer, ItemStack>> items) {
		items.clear();
		Collection<ServiceInfoSnapshot> servers = new HashSet<>();
		getCloudTasks().stream().forEach(
				task -> servers.addAll(CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices(task)));

		Map<ServiceInfoSnapshot, GameState> states = new HashMap<>();
		for (ServiceInfoSnapshot server : new HashSet<>(servers)) {
			try {
				GameState state = GameState.fromString(ServiceInfoSnapshotUtil.getState(server));
				if (state == null || state == GameState.UNKNOWN)
					throw new NullPointerException();
				states.put(server, state);
			} catch (Exception ex) {
				servers.remove(server);
			}
		}

		List<ItemSortingInfo> itemSortingInfos = new ArrayList<>();
		for (ServiceInfoSnapshot server : new HashSet<>(servers)) {
			String extraText = ServiceInfoSnapshotUtil.getExtra(server);

			int online = ServiceInfoSnapshotUtil.getOnlineCount(server);
			int maxPlayers = ServiceInfoSnapshotUtil.getMaxPlayers(server);
			try {
				JsonObject json = new Gson().fromJson(extraText, JsonObject.class);
				online = json.getAsJsonPrimitive("online").getAsInt();
				maxPlayers = json.getAsJsonPrimitive("max").getAsInt();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			GameState state = states.get(server);
			String motd = ServiceInfoSnapshotUtil.getMotd(server);
			if (motd == null || motd.contains("§cLoading...")) {
				servers.remove(server);
				states.remove(server);
				continue;
			}
			ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY);
			builder.unsafeStackSize(true);
			builder.setAmount(online);
			builder.setDisplayName(motd);
			builder.addLore("§7Spieler: " + online + "/" + maxPlayers);
			if (state == GameState.LOBBY) {
				builder.setDurability(DyeColor.LIME.getWoolData());
			} else if (state == GameState.INGAME) {
				builder.setDurability(DyeColor.ORANGE.getWoolData());
			} else if (state == GameState.STOPPING) {
				builder.setDurability(DyeColor.RED.getWoolData());
			} else if (state == GameState.UNKNOWN) {
				builder.setDurability(DyeColor.RED.getWoolData());
			}
			ItemStack item = builder.build();
			item = new ItemBuilder(item).getUnsafe()
					.setString("minigameServer", server.getServiceId().getUniqueId().toString()).builder()
					.getItemStack();
			ItemSortingInfo info = new ItemSortingInfo(item, online, maxPlayers, state);
			itemSortingInfos.add(info);
		}

		Collections.sort(itemSortingInfos);

//		itemSortingInfos.sort((o1, o2) -> o2.compareTo(o1));

		for (int slotId = 0; slotId < Math.min(SLOTS.length, itemSortingInfos.size()); slotId++) {
			int slot = SLOTS[slotId];
			ItemSortingInfo info = itemSortingInfos.get(slotId);
			Map<Integer, ItemStack> m = items.get(SORT[slotId]);
			if (m == null) {
				m = new HashMap<>();
				items.put(SORT[slotId], m);
			}
			m.put(slot, info.getItem());
		}
	}

	private void animate(User user, boolean instant) {

		Map<Integer, Map<Integer, ItemStack>> items = new HashMap<>();

		new Runnable(user, items, instant).runTaskTimer(Lobby.getInstance(), 1, 1);
	}

	private class Runnable extends BukkitRunnable {
		private Map<Integer, Map<Integer, ItemStack>> items;
		private User user;
		private ItemStack i1;
		private ItemStack i2;
		private Set<Integer> usedSlots = new HashSet<>();
		private boolean instant;
		private int count = 0;

		public Runnable(User user, Map<Integer, Map<Integer, ItemStack>> items, boolean instant) {
			this.user = user;
			this.instant = instant;
			this.items = items;
			i1 = Item.LIGHT_GRAY_GLASS_PANE.getItem(user);
			i2 = Item.DARK_GRAY_GLASS_PANE.getItem(user);
			if (instant) {
				user.playSound(Sound.NOTE_STICKS, 1, 1);
			}
		}

		private void setItems() {
			Map<Integer, ItemStack> m = items.get(count);
			if (m == null)
				return;
			for (int slot : m.keySet()) {
				handle.setItem(slot, m.get(slot));
				usedSlots.add(slot);
			}
		}

		private void resetItems() {
			for (int slot : usedSlots) {
				handle.setItem(slot, null);
			}
			usedSlots.clear();
			for (int id : items.keySet()) {
				Map<Integer, ItemStack> m = items.get(id);
				for (int slot : m.keySet()) {
					handle.setItem(slot, m.get(slot));
					usedSlots.add(slot);
				}
			}
		}

		@Override
		public void run() {
			if (user.getOpenInventory() != MinigameInventoryOld.this) {
				this.cancel();
				return;
			}
			if (count % 30 == 0) {
				calculateItems(items);
				if (count > 0)
					resetItems();
			}
			if (count <= 13) {
				setItems();
				if (!instant)
					user.playSound(Sound.NOTE_STICKS, 1, 1);
			}
			switch (count) {
			case 0:
				handle.setItem(4, getMinigameItem(user));
				break;
			case 1:
				handle.setItem(3, i1);
				handle.setItem(5, i1);
				break;
			case 2:
				handle.setItem(2, i1);
				handle.setItem(6, i1);
				break;
			case 3:
				handle.setItem(1, i1);
				handle.setItem(7, i1);
				break;
			case 4:
				handle.setItem(0, i1);
				handle.setItem(8, i1);
				break;
			case 5:
				handle.setItem(9, i2);
				handle.setItem(17, i2);
				break;
			case 6:
				handle.setItem(18, i1);
				handle.setItem(26, i1);
				break;
			case 7:
				handle.setItem(27, i2);
				handle.setItem(35, i2);
				break;
			case 8:
				handle.setItem(36, i1);
				handle.setItem(44, i1);
				break;
			case 9:
				handle.setItem(45, i1);
				handle.setItem(53, i1);
				break;
			case 10:
				handle.setItem(46, i1);
				handle.setItem(52, i1);
				break;
			case 11:
				handle.setItem(47, i2);
				handle.setItem(51, i2);
				break;
			case 12:
				handle.setItem(48, i2);
				handle.setItem(50, i2);
				break;
			case 13:
				handle.setItem(49, i2);
				break;
			default:
				break;
			}
			count++;
			if (instant && count <= 13) {
				run();
			}
		}
	}

	protected class ItemSortingInfo implements Comparable<ItemSortingInfo> {

		private ItemStack item;
		private Integer onPlayers;
		private Integer maxPlayers;
		private GameState state;

		public ItemSortingInfo(ItemStack item, int onPlayers, int maxPlayers, GameState state) {
			super();
			this.item = item;
			this.onPlayers = onPlayers;
			this.maxPlayers = maxPlayers;
			this.state = state;
		}

		@Override
		public int compareTo(ItemSortingInfo other) {
			int amt = 0;
			switch (state) {
			case LOBBY:
				if (other.state != GameState.LOBBY)
					return 1;
				amt = ((Integer) item.getAmount()).compareTo(other.item.getAmount());
				break;
			case INGAME:
				if (other.state == GameState.LOBBY)
					return -1;
				if (other.state != GameState.LOBBY && other.state != GameState.INGAME)
					return 1;
				amt = ((Integer) item.getAmount()).compareTo(other.item.getAmount());
				break;
			case STOPPING:
				if (other.state == GameState.LOBBY || other.state == GameState.INGAME)
					return -1;
				if (other.state == GameState.UNKNOWN)
					return 1;
				amt = ((Integer) item.getAmount()).compareTo(other.item.getAmount());
				break;
			case UNKNOWN:
				if (other.state != GameState.UNKNOWN)
					return -1;
				amt = ((Integer) item.getAmount()).compareTo(other.item.getAmount());
				break;
			}
			if (amt == 0) {
				if (onPlayers > other.onPlayers) {
					return -1;
				} else if (onPlayers < other.onPlayers) {
					return 1;
				}
				amt = maxPlayers.compareTo(other.maxPlayers);
				if (amt == 0) {
					amt = getDisplay().orElse("").compareTo(other.getDisplay().orElse(""));
				}

//				String display = getDisplay().orElse("");
//				amt = display.compareToIgnoreCase(other.getDisplay().orElse(""));
			}
			return amt;
		}

		private Optional<String> getDisplay() {
			if (item.hasItemMeta()) {
				return Optional.ofNullable(item.getItemMeta().getDisplayName());
			}
			return Optional.ofNullable(null);
		}

		public ItemStack getItem() {
			return new ItemBuilder(item).build();
		}
	}
}
