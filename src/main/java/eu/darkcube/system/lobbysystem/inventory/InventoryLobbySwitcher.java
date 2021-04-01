package eu.darkcube.system.lobbysystem.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceId;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.dytanic.cloudnet.wrapper.Wrapper;
import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.abstraction.Inventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;

public class InventoryLobbySwitcher extends Inventory {

//	private static final int[] SLOTS;
//	private static final int[] SORT;
//
//	static {
//		int center = s(1, 5);
//		List<Integer> slots = new ArrayList<>();
//		for (int r = 2; r <= 4; r++) {
//			for (int i = 2; i <= 8; i++) {
//				slots.add(s(r, i));
//			}
//		}
//		SLOTS = new int[slots.size()];
//		SORT = new int[SLOTS.length];
//		for (int i = 0; i < SLOTS.length; i++) {
//			SLOTS[i] = slots.get(i);
//			SORT[i] = dist(center, SLOTS[i]);
//		}
//	}
//	
	public InventoryLobbySwitcher(User user) {
		super(Bukkit.createInventory(null, 6 * 9, Item.INVENTORY_LOBBY_SWITCHER.getDisplayName(user)),
				InventoryType.LOBBY_SWITCHER);
	}

	@Override
	public void playAnimation(User user) {
		animate(user, false);
	}

	@Override
	public void skipAnimation(User user) {
		animate(user, true);
	}

	private static final int[] SLOTS = new int[] {
			31, 30, 32, 29, 33, 28, 34,
			22, 21, 23, 20, 24, 19, 25,
			40, 39, 41, 38, 42, 37, 43
	};
	private static final int[] SORT = new int[] {
			02, 03, 03, 04, 04, 05, 05,
			01, 02, 02, 03, 03, 04, 04,
			03, 04, 04, 05, 05, 06, 06
	};

	private void animate(User user, boolean instant) {
		ServiceId service = Wrapper.getInstance().getServiceId();
		String taskName = service.getTaskName();
		List<ServiceInfoSnapshot> lobbies = CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices(taskName)
				.stream().filter(ServiceInfoSnapshot::isConnected)
				.filter(s -> s.getProperty(BridgeServiceProperty.IS_ONLINE).orElse(false))
				.sorted(new Comparator<ServiceInfoSnapshot>() {
					@Override
					public int compare(ServiceInfoSnapshot o1, ServiceInfoSnapshot o2) {
						return ((Integer) o1.getServiceId().getTaskServiceId())
								.compareTo(o2.getServiceId().getTaskServiceId());
					}
				}).collect(Collectors.toList());

		if (lobbies == null || lobbies.isEmpty()) {
			ItemStack item = new ItemStack(Material.BARRIER);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("§cUnable to load lobbies!");
			handle.setItem(SLOTS[0], item);
			return;
		}
		final ServiceInfoSnapshot thisLobby = Wrapper.getInstance().getCurrentServiceInfoSnapshot();
		final int lobbyCount = lobbies.size();

		final Map<Integer, Map<Integer, ServiceInfoSnapshot>> slots = new HashMap<>();

		final List<Integer> usedSlots = new ArrayList<>();
		for (int i = 0; i < Math.min(SLOTS.length, lobbyCount); i++) {
			Map<Integer, ServiceInfoSnapshot> m = slots.get(SORT[i]);
			if (m == null) {
				m = new HashMap<>();
				slots.put(SORT[i], m);
			}
			usedSlots.add(SLOTS[i]);
			m.put(SLOTS[i], lobbies.get(i));
		}
		Collections.sort(usedSlots);

		final Map<Integer, Map<Integer, ItemStack>> slots1 = new HashMap<>();

		int pointer = 0;
		for (int slot : usedSlots) {
			for (int count : slots.keySet()) {
				Map<Integer, ItemStack> map = slots1.get(count);
				if (map == null) {
					map = new HashMap<>();
					slots1.put(count, map);
				}
				for (int oslot : slots.get(count).keySet()) {
					if (oslot != slot) {
						continue;
					}
					ServiceInfoSnapshot s = lobbies.get(pointer++);
					ItemStack item = Item.INVENTORY_LOBBY_SWITCHER_OTHER.getItem(user);
					ItemMeta meta = item.getItemMeta();
					String id = s.getServiceId().getName();
					if (id.length() > 0) {
						id = Character.toUpperCase(id.charAt(0)) + id.substring(1, id.length()).replace("-", " ");
					}
					if (s.getServiceId().equals(thisLobby.getServiceId())) {
						item = Item.INVENTORY_LOBBY_SWITCHER_CURRENT.getItem(user);
						meta.setDisplayName("§c" + id);
					} else {
						meta.setDisplayName("§a" + id);
					}
					item.setItemMeta(meta);
					item = new ItemBuilder(item).getUnsafe().setString("server", s.getServiceId().getName()).builder()
							.getItemStack();
					map.put(slot, item);
				}
			}
		}

		user.playSound(Sound.NOTE_STICKS, 1, 1);
		new BukkitRunnable() {
			private ItemStack i1 = Item.LIGHT_GRAY_GLASS_PANE.getItem(user);
			private ItemStack i2 = Item.DARK_GRAY_GLASS_PANE.getItem(user);
			private int count = 0;
			private boolean lobbiesDone = false;

			private void setLobby() {
				Map<Integer, ItemStack> m = slots1.get(count);
				if (m == null)
					return;
				for (int slot : m.keySet()) {
					handle.setItem(slot, m.get(slot));
				}
				slots1.remove(count);
				if (slots1.isEmpty()) {
					lobbiesDone = true;
				}
			}

			@Override
			public void run() {
				if ((count >= 13 && lobbiesDone) || count >= 22
						|| user.getOpenInventory() != InventoryLobbySwitcher.this) {
					this.cancel();
					return;
				}
				if (!instant)
					user.playSound(Sound.NOTE_STICKS, 1, 1);
				setLobby();
				switch (count) {
				case 0:
					handle.setItem(3, i1);
					handle.setItem(4, Item.INVENTORY_LOBBY_SWITCHER.getItem(user));
					handle.setItem(5, i1);
					handle.setItem(13, i1);
					break;
				case 1:
					handle.setItem(2, i1);
					handle.setItem(12, i1);
					handle.setItem(14, i1);
					handle.setItem(6, i1);
					break;
				case 2:
					handle.setItem(1, i1);
					handle.setItem(11, i2);
					handle.setItem(15, i2);
					handle.setItem(7, i1);
					break;
				case 3:
					handle.setItem(0, i1);
					handle.setItem(10, i2);
					handle.setItem(16, i2);
					handle.setItem(8, i1);
					break;
				case 4:
					handle.setItem(9, i2);
					handle.setItem(17, i2);
					break;
				case 5:
					handle.setItem(18, i2);
					handle.setItem(26, i2);
					break;
				case 6:
					handle.setItem(27, i2);
					handle.setItem(35, i2);
					break;
				case 7:
					handle.setItem(36, i1);
					handle.setItem(44, i1);
					break;
				case 8:
					handle.setItem(45, i1);
					handle.setItem(53, i1);
					break;
				case 9:
					handle.setItem(46, i1);
					handle.setItem(52, i1);
					break;
				case 10:
					handle.setItem(47, i2);
					handle.setItem(51, i2);
					break;
				case 11:
					handle.setItem(48, i2);
					handle.setItem(50, i2);
					break;
				case 12:
					handle.setItem(49, i2);
					break;
				default:
					break;
				}
				count++;
				if (instant)
					run();
			}
		}.runTaskTimer(Lobby.getInstance(), 3, 1);
	}
}