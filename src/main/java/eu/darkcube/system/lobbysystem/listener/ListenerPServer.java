package eu.darkcube.system.lobbysystem.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryPServerOwn;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;

public class ListenerPServer extends BaseListener {

	@EventHandler
	public void handle(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		ItemStack item = e.getCurrentItem();
		if (item == null) {
			return;
		}
//		Player p = (Player) e.getWhoClicked();
//		User user = UserWrapper.getUser(p.getUniqueId());
		String itemid = Item.getItemId(item);
		if (itemid == null) {
			return;
		}
		if (!itemid.equals(Item.INVENTORY_PSERVER_SLOT_EMPTY.getItemId())) {
			return;
		}
		int slot = new ItemBuilder(item).getUnsafe().getInt(InventoryPServerOwn.META_KEY_SLOT);
		System.out.println(slot);
	}
}
