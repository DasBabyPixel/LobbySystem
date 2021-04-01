package eu.darkcube.system.lobbysystem.inventory.pserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.inventory.abstraction.PagedInventoryOld;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager.PServerUserSlot;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;

public class InventoryPServerSlot extends PagedInventoryOld {

	@SuppressWarnings("unused")
	private PServerUserSlot slot;
	private Map<User, Map<Integer, ItemStack>> contents = new HashMap<>();
	private Set<User> toRemove = new HashSet<>();

	public InventoryPServerSlot(User user, PServerUserSlot slot) {
		super(Item.PSERVER_SLOT.getDisplayName(user), InventoryType.PSERVER_SLOT, Item.PSERVER_SLOT);
		this.slot = slot;
	}

	@Override
	protected Map<Integer, ItemStack> getContents(User user) {
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
	protected Map<Integer, ItemStack> getStaticContents(User user) {
		return new HashMap<>();
	}

	@Override
	protected void onOpen(User user) {
		BukkitRunnable r = new BukkitRunnable() {
			@Override
			public void run() {
				Map<Integer, ItemStack> m = new HashMap<>();
				
				
				
				if (!toRemove.contains(user)) {
					contents.put(user, m);
				}
			}
		};
		r.runTaskAsynchronously(Lobby.getInstance());
	}

	@Override
	protected void onClose(User user) {
		if (contents.containsKey(user)) {
			contents.remove(user);
		} else {
			toRemove.add(user);
		}
	}
}
