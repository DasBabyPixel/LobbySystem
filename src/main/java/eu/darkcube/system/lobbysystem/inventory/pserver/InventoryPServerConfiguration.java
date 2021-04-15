package eu.darkcube.system.lobbysystem.inventory.pserver;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import eu.darkcube.system.lobbysystem.inventory.abstraction.DefaultPagedInventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager.PServerUserSlot;
import eu.darkcube.system.lobbysystem.user.User;

public class InventoryPServerConfiguration extends DefaultPagedInventory {

	public final PServerUserSlot psslot;

	public InventoryPServerConfiguration(User user, PServerUserSlot psslot) {
		super(PServerDataManager.getDisplayItem(user, psslot).getDisplayname(), InventoryType.PSERVER_CONFIGURATION);
		this.psslot = psslot;
	}

	@Override
	protected Map<Integer, ItemStack> contents(User user) {
		Map<Integer, ItemStack> m = new HashMap<>();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	@Override
	protected void insertDefaultItems(InventoryManager manager) {
		super.insertDefaultItems(manager);
		manager.setFallbackItem(s(1, 5), PServerDataManager.getDisplayItem(manager.user, psslot).build());
	}

}
