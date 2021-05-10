package eu.darkcube.system.lobbysystem.inventory.pserver;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import eu.darkcube.system.lobbysystem.inventory.abstraction.DefaultPagedInventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.listener.ListenerPServer;
import eu.darkcube.system.lobbysystem.pserver.PServerDataManager;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.lobbysystem.util.Message;
import eu.darkcube.system.lobbysystem.util.SkullCache;
import eu.darkcube.system.pserver.common.PServer;
import eu.darkcube.system.pserver.common.PServer.State;
import eu.darkcube.system.pserver.common.PServerProvider;
import eu.darkcube.system.pserver.wrapper.event.PServerUpdateEvent;

public class InventoryPServer extends DefaultPagedInventory {

   public static final String ITEMID = "lobbysystem.pserver.publiclist";
   public static final String META_KEY_PSERVER = "lobbysystem.pserver.id";
   private Listener listener;

   public InventoryPServer(User user) {
      super(Item.PSERVER_MAIN_ITEM.getDisplayName(user), InventoryType.PSERVER);
      this.listener = new Listener();
      this.listener.register();
   }

   @Override
   protected Map<Integer, ItemStack> contents(User user) {
      SortedMap<Long, ItemStack> items = new TreeMap<>();

      for (PServer ps : PServerProvider.getInstance().getPServers()) {
         if (ps.getState() != State.RUNNING || !ListenerPServer.mayJoin(user, ps)) {
            continue;
         }
         boolean publicServer = ps.isPublic();
         int online = ps.getOnlinePlayers();
         long ontime = ps.getOntime();
         UUID owner = ps.getOwners().stream().findAny().orElse(null);

         ItemBuilder b = null;
         boolean skull = false;

         if (ps.isGamemode()) {
            b = PServerDataManager.getDisplayItemGamemode(user, ps.getTaskName());
         }
         if (b == null) {
            b = new ItemBuilder(owner == null ? Material.BARRIER : Material.SKULL_ITEM)
                  .setDurability((short) SkullType.PLAYER.ordinal());
            skull = b.getMaterial() == Material.SKULL_ITEM;
         }
         if (skull) {
            b.setMeta(SkullCache.getCachedItem(owner).getItemMeta());
         }
         b.unsafeStackSize(true).setAmount(online);
         b.setDisplayName(Message.PSERVER_ITEM_TITLE.getMessage(user, ps.getServerName()));
         b.addLore(publicServer ? Message.CLICK_TO_JOIN.getMessage(user) : Message.PSERVER_NOT_PUBLIC.getMessage(user));
         b.build();
         b.setMeta(null);
         Item.setItemId(b, ITEMID);
         b.getUnsafe().setString(META_KEY_PSERVER, ps.getId().toString());

         items.put(ontime, b.build());
      }
      Map<Integer, ItemStack> m = new HashMap<>();

      int slot = 0;
      for (long ontime : items.keySet()) {
         m.put(slot, items.get(ontime));
         slot++;
      }
      return m;
   }

   @Override
   protected void onClose0(User user) {
      this.listener.unregister();
   }

   public class Listener {

      public void register() {
         CloudNetDriver.getInstance().getEventManager().registerListener(this);
      }

      public void unregister() {
         CloudNetDriver.getInstance().getEventManager().unregisterListener(this);
      }

      @EventListener
      public void handle(@SuppressWarnings("unused") PServerUpdateEvent event) {
         recalculateAll();
      }
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
