package eu.darkcube.system.lobbysystem.inventory.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;

public abstract class DefaultPagedInventory extends PagedInventory {

	protected Map<User, Map<Integer, ItemStack>> contents = new HashMap<>();
	protected Collection<User> toRemove = new ArrayList<>();
	protected Object lock = new Object();

	public DefaultPagedInventory(String title, InventoryType type) {
		super(title, 6 * 9, type);
	}

	public DefaultPagedInventory(String title, Item mainItem, InventoryType type, int... pageSlots) {
		super(title, mainItem, 6 * 9, type, pageSlots);
	}

	public DefaultPagedInventory(String title, Item mainItem, InventoryType type) {
		super(title, mainItem, 6 * 9, type);
	}

	@Override
	protected final Map<Integer, ItemStack> getContents(User user) {
		if (contents.containsKey(user)) {
			return contents.get(user);
		}
		Map<Integer, ItemStack> m = new HashMap<>();
		for (int i = 0; i < getPageSize(); i++) {
			m.put(i, Item.LOADING.getItem(user));
		}
		return m;
	}

	@Override
	protected final void onClose(User user) {
		synchronized (lock) {
			if (contents.containsKey(user)) {
				contents.remove(user);
			} else {
				toRemove.add(user);
			}
		}
	}

	protected abstract Map<Integer, ItemStack> contents(User user);

	@Override
	protected final void onOpen(User user) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Map<Integer, ItemStack> m = contents(user);
				synchronized (lock) {
					if (toRemove.contains(user)) {
						toRemove.remove(user);
					} else {
						contents.put(user, m);
					}
				}
				update(user);
			}
		}.runTaskAsynchronously(Lobby.getInstance());
	}

	@Override
	protected Map<Integer, ItemStack> getStaticContents(User user) {
		return new HashMap<>();
	}

	@Override
	protected void insertDefaultItems(InventoryManager manager) {
		ItemStack l = Item.LIGHT_GRAY_GLASS_PANE.getItem(manager.user);
		ItemStack d = Item.DARK_GRAY_GLASS_PANE.getItem(manager.user);
		manager.setFallbackItem(s0(1, 1), l);
		manager.setFallbackItem(s0(2, 1), l);
		manager.setFallbackItem(s0(3, 1), l);
		manager.setFallbackItem(s0(4, 1), l);
		manager.setFallbackItem(s0(5, 1), mainItem != null ? mainItem.getItem(manager.user) : d);
		manager.setFallbackItem(s0(6, 1), l);
		manager.setFallbackItem(s0(7, 1), l);
		manager.setFallbackItem(s0(8, 1), l);
		manager.setFallbackItem(s0(9, 1), l);

		manager.setFallbackItem(s0(1, 2), d);
		manager.setFallbackItem(s0(2, 2), d);
		manager.setFallbackItem(s0(3, 2), d);
		manager.setFallbackItem(s0(4, 2), l);
		manager.setFallbackItem(s0(5, 2), l);
		manager.setFallbackItem(s0(6, 2), l);
		manager.setFallbackItem(s0(7, 2), d);
		manager.setFallbackItem(s0(8, 2), d);
		manager.setFallbackItem(s0(9, 2), d);

		manager.setFallbackItem(s0(1, 3), l);
		manager.setFallbackItem(s0(2, 3), d);
		manager.setFallbackItem(s0(3, 3), d);
		manager.setFallbackItem(s0(4, 3), d);
		manager.setFallbackItem(s0(5, 3), l);
		manager.setFallbackItem(s0(6, 3), d);
		manager.setFallbackItem(s0(7, 3), d);
		manager.setFallbackItem(s0(8, 3), d);
		manager.setFallbackItem(s0(9, 3), l);

		manager.setFallbackItem(s0(1, 4), d);
		manager.setFallbackItem(s0(2, 4), l);
		manager.setFallbackItem(s0(3, 4), d);
		manager.setFallbackItem(s0(4, 4), l);
		manager.setFallbackItem(s0(5, 4), d);
		manager.setFallbackItem(s0(6, 4), l);
		manager.setFallbackItem(s0(7, 4), d);
		manager.setFallbackItem(s0(8, 4), l);
		manager.setFallbackItem(s0(9, 4), d);

		manager.setFallbackItem(s0(1, 5), l);
		manager.setFallbackItem(s0(2, 5), d);
		manager.setFallbackItem(s0(3, 5), l);
		manager.setFallbackItem(s0(4, 5), d);
		manager.setFallbackItem(s0(5, 5), l);
		manager.setFallbackItem(s0(6, 5), d);
		manager.setFallbackItem(s0(7, 5), l);
		manager.setFallbackItem(s0(8, 5), d);
		manager.setFallbackItem(s0(9, 5), l);

		manager.setFallbackItem(s0(1, 6), l);
		manager.setFallbackItem(s0(2, 6), l);
		manager.setFallbackItem(s0(3, 6), d);
		manager.setFallbackItem(s0(4, 6), d);
		manager.setFallbackItem(s0(5, 6), d);
		manager.setFallbackItem(s0(6, 6), d);
		manager.setFallbackItem(s0(7, 6), d);
		manager.setFallbackItem(s0(8, 6), l);
		manager.setFallbackItem(s0(9, 6), l);

		manager.arrowSlots.put(ArrowType.PREV, new Integer[] {
				s(3, 1), s(4, 1), s(5, 1)
		});
		manager.arrowSlots.put(ArrowType.NEXT, new Integer[] {
				s(3, 9), s(4, 9), s(5, 9)
		});
	}
}
