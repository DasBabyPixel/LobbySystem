package eu.darkcube.system.lobbysystem.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import eu.darkcube.system.lobbysystem.inventory.abstraction.Inventory;
import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryGameServerSelection;
import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryNewPServerSlot;
import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryPServer;
import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryPServerConfiguration;
import eu.darkcube.system.lobbysystem.inventory.pserver.InventoryPServerOwn;
import eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelectionWoolBattle;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager.PServerUserSlot;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.user.UserWrapper;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServerProvider;
import eu.darkcube.system.pserver.common.UniqueId;
import eu.darkcube.system.pserver.common.UniqueIdProvider;

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
		ItemBuilder itemb = new ItemBuilder(item);
		String itemid = Item.getItemId(item);
		if (itemid == null) {
			return;
		}
		User user = UserWrapper.getUser(e.getWhoClicked().getUniqueId());

		if (itemid.equals(Item.INVENTORY_PSERVER_SLOT_EMPTY.getItemId())) {
			int slot = itemb.getUnsafe().getInt(InventoryPServerOwn.META_KEY_SLOT);
			PServerUserSlot psslot = user.getSlots().getSlot(slot);
			user.setOpenInventory(new InventoryNewPServerSlot(user, psslot, slot + 1));
		} else {
			Inventory inv = user.getOpenInventory();
			if (inv instanceof InventoryNewPServerSlot) {
				InventoryNewPServerSlot cinv = (InventoryNewPServerSlot) inv;
				if (itemid.equals(Item.GAME_PSERVER.getItemId())) {
					user.setOpenInventory(new InventoryGameServerSelection(user, cinv.psslot, cinv.slot));
				}
			} else if (inv instanceof InventoryGameServerSelection) {
				InventoryGameServerSelection cinv = (InventoryGameServerSelection) inv;
				if (itemid.equals(Item.GAMESERVER_SELECTION_WOOLBATTLE.getItemId())) {
					user.setOpenInventory(new InventoryGameServerSelectionWoolBattle(user, cinv.psslot, cinv.slot));
				}
			} else if (inv instanceof eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection) {
				eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection cinv = (eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection) inv;
				if (itemid.equals(
						eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection.ITEMID)) {

					JsonObject extra = new Gson().fromJson(itemb.getUnsafe()
							.getString(
									eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection.GAMESERVER_META_KEY),
							JsonObject.class);
					ServiceTask task = CloudNetDriver.getInstance()
							.getServiceTaskProvider()
							.getServiceTask(extra.get(
									eu.darkcube.system.lobbysystem.inventory.pserver.gameserver.InventoryGameServerSelection.SERVICETASK)
									.getAsString());
					if (task == null) {
						return;
					}
					JsonObject data = cinv.psslot.getData();
					data.addProperty("task", task.getName());
					cinv.psslot.load(UniqueIdProvider.getInstance().newUniqueId());
					user.setOpenInventory(new InventoryPServerConfiguration(user, cinv.psslot));
				}
			} else if (inv instanceof InventoryPServer) {
				if (itemid.equals(InventoryPServer.ITEMID)) {
					String psid = itemb.getUnsafe().getString(InventoryPServer.META_KEY_PSERVER);
					PServer ps = PServerProvider.getInstance().getPServer(new UniqueId(psid));
					if (ps == null) {
						user.setOpenInventory(new InventoryPServer(user));
						return;
					}
				}
			}
		}
	}
}
