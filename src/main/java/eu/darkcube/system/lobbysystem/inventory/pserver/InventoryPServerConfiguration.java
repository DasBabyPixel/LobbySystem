package eu.darkcube.system.lobbysystem.inventory.pserver;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import eu.darkcube.system.lobbysystem.inventory.abstraction.DefaultPagedInventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager.PServerUserSlots.PServerUserSlot;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;

public class InventoryPServerConfiguration extends DefaultPagedInventory {

	public final PServerUserSlot psslot;

	public InventoryPServerConfiguration(User user, PServerUserSlot psslot) {
		super(getDisplayName(user, psslot), InventoryType.PSERVER_CONFIGURATION);
		this.psslot = psslot;
	}

	private static String getDisplayName(User user, PServerUserSlot psslot) {
		ItemBuilder item = PServerDataManager.getDisplayItem(user, psslot);
		return item == null ? null : item.getDisplayname();
	}

	@Override
	protected Map<Integer, ItemStack> contents(User user) {
		Map<Integer, ItemStack> m = new HashMap<>();
		m.put(15, Item.PSERVER_DELETE.getItem(user));
		m.put(19, Item.START_PSERVER.getItem(user));
		return m;
	}

	@Override
	protected void insertDefaultItems(InventoryManager manager) {
		super.insertDefaultItems(manager);
		manager.setFallbackItem(s(1, 5), PServerDataManager.getDisplayItem(manager.user, psslot).build());
	}

}
