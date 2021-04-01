package eu.darkcube.system.lobbysystem.inventory.pserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.abstraction.DefaultPagedInventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.lobbysystem.util.Message;
import eu.darkcube.system.lobbysystem.util.SkullCache;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServer.State;
import eu.darkcube.system.pserver.common.PServerProvider;

public class InventoryPServer extends DefaultPagedInventory {

	public static final String META_KEY_PSERVER = "lobbysystem.pserver.id";

	public InventoryPServer(User user) {
		super(Item.PSERVER_MAIN_ITEM.getDisplayName(user), InventoryType.PSERVER);
	}

	private Map<User, Map<Integer, ItemStack>> contents = new HashMap<>();

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
	protected void onOpen(User user) {
		new BukkitRunnable() {
			@Override
			public void run() {
				SortedMap<Long, ItemStack> items = new TreeMap<>();

				for (PServer ps : PServerProvider.getInstance().getPServers()) {
					if (ps.getState() != State.RUNNING)
						continue;
					boolean publicServer = ps.isPublic();
					int online = ps.getOnlinePlayers();
					long ontime = ps.getOntime();
					UUID owner = ps.getOwners().stream().findAny().orElse(null);

					ItemStack i = new ItemStack(owner == null ? Material.BARRIER : Material.SKULL_ITEM, 1,
							(short) SkullType.PLAYER.ordinal());
					i.setAmount(online);
					SkullMeta meta = (SkullMeta) SkullCache.getCachedItem(owner).getItemMeta();
					ItemMeta imeta = meta;
					if (imeta == null) {
						imeta = i.getItemMeta();
					}

					imeta.setDisplayName(Message.PSERVER_ITEM_TITLE.getMessage(user, ps.getServerName()));
					imeta.setLore(Arrays.asList(publicServer ? Message.CLICK_TO_JOIN.getMessage(user)
							: Message.PSERVER_NOT_PUBLIC.getMessage(user)));

					i.setItemMeta(imeta);

					i = new ItemBuilder(i).getUnsafe()
							.setString(META_KEY_PSERVER, ps.getId().toString())
							.builder()
							.build();

					items.put(ontime, i);
				}
				Map<Integer, ItemStack> m = new HashMap<>();

				int slot = 0;
				for (long ontime : items.keySet()) {
					m.put(slot, items.get(ontime));
					slot++;
				}
				contents.put(user, m);
				update(user);
			}
		}.runTaskAsynchronously(Lobby.getInstance());
	}

	@Override
	protected void onClose(User user) {
		contents.remove(user);
	}

	@Override
	protected Map<Integer, ItemStack> getStaticContents(User user) {
		Map<Integer, ItemStack> m = new HashMap<>();
		return m;
	}

	@Override
	protected void insertDefaultItems(InventoryManager manager) {
		super.insertDefaultItems(manager);
		manager.setFallbackItem(s(1, 3), Item.LIME_GLASS_PANE.getItem(manager.user));
		manager.setFallbackItem(s(1, 4), Item.INVENTORY_PSERVER_PUBLIC.getItem(manager.user));
		manager.setFallbackItem(s(1, 5), Item.LIME_GLASS_PANE.getItem(manager.user));
		manager.setFallbackItem(s(1, 6), Item.INVENTORY_PSERVER_PRIVATE.getItem(manager.user));

	}
}
