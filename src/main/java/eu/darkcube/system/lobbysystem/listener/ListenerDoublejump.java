package eu.darkcube.system.lobbysystem.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.user.UserWrapper;

public class ListenerDoublejump extends BaseListener {

	private Map<Player, Integer> cooldown = new HashMap<>();
	private Set<Player> waiting = new HashSet<>();

	@EventHandler
	public void handle(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		User user = UserWrapper.getUser(p.getUniqueId());
		if (user.isBuildMode()) {
			return;
		}
		if(!e.isFlying())
			return;
		e.setCancelled(true);
		if (!cooldown.containsKey(p)) {
			cooldown.put(p, 50);
			Vector v = p.getLocation().getDirection();
			v.normalize().multiply(3);
			v.setY(Math.min(2, Math.max(1, v.getY())));
			p.playSound(p.getLocation(), Sound.GHAST_FIREBALL, 10, 1);
			p.setVelocity(v);
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!cooldown.containsKey(p)) {
						p.setAllowFlight(true);
						cancel();
						return;
					}
					if(p.isFlying())
						return;
					cooldown.put(p, cooldown.get(p) - 1);
					p.setExp(1 - (cooldown.get(p) / 80F));
					if (cooldown.get(p) <= 0) {
						cooldown.remove(p);
						waiting.remove(p);
					}
				}
			}.runTaskTimer(Lobby.getInstance(), 1, 1);
		} else if (!waiting.contains(p)) {
			cooldown.put(p, cooldown.get(p) + 50);
			if (cooldown.get(p) > 80)
				waiting.add(p);
			Vector v = p.getLocation().getDirection();
			v.normalize().multiply(3);
			v.setY(Math.min(2, Math.max(1, v.getY())));
			p.playSound(p.getLocation(), Sound.GHAST_FIREBALL, 10, 1);
			p.setVelocity(v);
		}
		if (waiting.contains(p)) {
			p.setAllowFlight(false);
			new BukkitRunnable() {
				@Override
				public void run() {
					((CraftPlayer) p).getHandle().abilities.canFly = true;
				}
			}.runTaskLater(Lobby.getInstance(), 5);
		}
	}
}