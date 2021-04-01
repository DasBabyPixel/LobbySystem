package eu.darkcube.system.lobbysystem.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.InventoryWoolBattle;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.user.UserWrapper;
import eu.darkcube.system.npcapi.Emote;
import eu.darkcube.system.npcapi.NPC;
import eu.darkcube.system.npcapi.NPCInteractEvent;
import eu.darkcube.system.npcapi.NPCInteractEvent.Action;

public class ListenerWoolBattleNPC extends BaseListener {

	@EventHandler
	public void handle(NPCInteractEvent e) {
		NPC npc = e.getNPC();
		if (npc.equals(Lobby.getInstance().getWoolBattleNPC())) {
			if (e.getAction() == Action.LEFT_CLICK) {
				List<Emote> emotes = new ArrayList<>(Arrays.asList(Emote.values()));
				emotes.remove(Emote.INFINITY_SIT);
				npc.sendEmote(emotes.get(new Random().nextInt(emotes.size())));
			} else if (e.getAction() == Action.RIGHT_CLICK) {
				Player p = e.getClicker();
				User user = UserWrapper.getUser(p.getUniqueId());
				user.setOpenInventory(new InventoryWoolBattle(user));
			}
		}
	}
}
