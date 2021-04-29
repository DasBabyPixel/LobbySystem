package eu.darkcube.system.lobbysystem.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.ext.bridge.player.ICloudOfflinePlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServerProvider;
import eu.darkcube.system.pserver.wrapper.event.PServerAddEvent;
import eu.darkcube.system.pserver.wrapper.event.PServerAddOwnerEvent;
import eu.darkcube.system.pserver.wrapper.event.PServerRemoveEvent;
import eu.darkcube.system.pserver.wrapper.event.PServerRemoveOwnerEvent;
import net.minecraft.server.v1_8_R3.ItemStack;

public class SkullCache {

	private static Map<UUID, ItemStack> cache = new HashMap<>();

	public static void loadToCache(UUID ownerUUID, String ownerName) {
		org.bukkit.inventory.ItemStack i = new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1,
				(short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) i.getItemMeta();
		meta.setOwner(ownerName);
		i.setItemMeta(meta);
		cache.put(ownerUUID, CraftItemStack.asNMSCopy(i));
	}

	public static void unloadFromCache(UUID ownerUUID) {
		cache.remove(ownerUUID);
	}

	public static org.bukkit.inventory.ItemStack getCachedItem(UUID ownerUUID) {
		return CraftItemStack.asBukkitCopy(cache.get(ownerUUID));
	}

	private static SkullCache sc = new SkullCache();

	public static void register() {
		CloudNetDriver.getInstance().getEventManager().registerListener(sc);
		cache.clear();
		IPlayerManager pm = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
		PServerProvider.getInstance().getPServers().forEach(ps -> {
			ps.getOwners().forEach(owner -> {
				loadToCache(owner, pm.getOfflinePlayer(owner).getName());
			});
		});
	}

	public static void unregister() {
		CloudNetDriver.getInstance().getEventManager().unregisterListener(sc);
	}

	@EventListener
	public void handle(PServerAddEvent e) {
		AsyncExecutor.service().submit(() -> {
			IPlayerManager pm = CloudNetDriver.getInstance()
					.getServicesRegistry()
					.getFirstService(IPlayerManager.class);
			for (UUID owner : e.getPServer().getOwners()) {
				loadToCache(owner, pm.getOfflinePlayer(owner).getName());
			}
		});
	}

	@EventListener
	public void handle(PServerRemoveEvent e) {
		AsyncExecutor.service().submit(() -> {
			Set<UUID> uuids = new HashSet<>();
			PServerProvider.getInstance()
					.getPServers()
					.stream()
					.filter(ps -> ps != e.getPServer())
					.map(PServer::getOwners)
					.forEach(uuids::addAll);
			for (UUID owner : e.getPServer().getOwners()) {
				if (!uuids.contains(owner)) {
					unloadFromCache(owner);
				}
			}
		});
	}

	@EventListener
	public void handle(PServerAddOwnerEvent e) {
		AsyncExecutor.service().submit(() -> {
			ICloudOfflinePlayer offp = CloudNetDriver.getInstance()
					.getServicesRegistry()
					.getFirstService(IPlayerManager.class)
					.getOfflinePlayer(e.getOwner());
			String name = offp != null ? offp.getName() : Bukkit.getOfflinePlayer(e.getOwner()).getName();
			loadToCache(e.getOwner(), name);
		});
	}

	@EventListener
	public void handle(PServerRemoveOwnerEvent e) {
		AsyncExecutor.service().submit(() -> {
			Set<UUID> uuids = new HashSet<>();
			PServerProvider.getInstance()
					.getPServers()
					.stream()
					.filter(ps -> ps != e.getPServer())
					.map(PServer::getOwners)
					.forEach(uuids::addAll);
			if (!uuids.contains(e.getOwner())) {
				unloadFromCache(e.getOwner());
			}
		});
	}
}
