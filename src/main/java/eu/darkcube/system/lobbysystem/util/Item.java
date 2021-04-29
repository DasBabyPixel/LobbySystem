package eu.darkcube.system.lobbysystem.util;

import static eu.darkcube.system.lobbysystem.util.ItemBuilder.*;
import static org.bukkit.Material.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import eu.darkcube.system.lobbysystem.gadget.Gadget;
import eu.darkcube.system.lobbysystem.user.User;

public enum Item {

	INVENTORY_COMPASS(item(Material.COMPASS)),

	INVENTORY_GADGET(item(ENDER_CHEST)),

	INVENTORY_LOBBY_SWITCHER(item(NETHER_STAR)),
	INVENTORY_LOBBY_SWITCHER_CURRENT(item(STORAGE_MINECART)),
	INVENTORY_LOBBY_SWITCHER_OTHER(item(MINECART)),

	INVENTORY_SETTINGS(item(REDSTONE_COMPARATOR)),
	INVENTORY_SETTINGS_ANIMATIONS_ON(item(BLAZE_POWDER)),
	INVENTORY_SETTINGS_ANIMATIONS_OFF(item(BLAZE_POWDER)),
	INVENTORY_SETTINGS_SOUNDS_ON(item(GOLD_RECORD)),
	INVENTORY_SETTINGS_SOUNDS_OFF(item(GOLD_RECORD)),

	INVENTORY_COMPASS_SPAWN(item(NETHER_STAR)),
	INVENTORY_COMPASS_SMASH(item(FIREBALL)),
	INVENTORY_COMPASS_WOOLBATTLE(item(BOW)),
	INVENTORY_COMPASS_MINERS(item(DIAMOND_PICKAXE).addFlag(ItemFlag.HIDE_ATTRIBUTES)),

	GADGET_HOOK_ARROW(
			item(BOW).setUnbreakable(true)
					.addFlag(ItemFlag.HIDE_UNBREAKABLE)
					.addEnchant(Enchantment.ARROW_INFINITE, 1)
					.addFlag(ItemFlag.HIDE_ENCHANTS)),
	GADGET_HOOK_ARROW_ARROW(item(ARROW)),

	GADGET_GRAPPLING_HOOK(item(FISHING_ROD).setUnbreakable(true).addFlag(ItemFlag.HIDE_UNBREAKABLE)),

	LIGHT_GRAY_GLASS_PANE(item(STAINED_GLASS_PANE).setDurability(7)),
	DARK_GRAY_GLASS_PANE(item(STAINED_GLASS_PANE).setDurability(15)),
	LIME_GLASS_PANE(item(STAINED_GLASS_PANE).setDurability(5)),

	INVENTORY_PSERVER_PUBLIC(item(PAPER)),
	INVENTORY_PSERVER_PRIVATE(item(COMMAND)),

	INVENTORY_PSERVER_SLOT_EMPTY(item(STONE_BUTTON)),
	INVENTORY_PSERVER_SLOT_NOT_BOUGHT(item(FIREWORK_CHARGE).addFlag(ItemFlag.HIDE_POTION_EFFECTS)),

	PSERVER_MAIN_ITEM(item(COMMAND)),
	NEXT(item(ARROW)),
	PREV(item(ARROW)),
	PSERVER_OWN_MENU(item(COMMAND)),
	INVENTORY_PSERVER(item(COMMAND)),
	GAMESERVER_SELECTION_WOOLBATTLE(item(BOW)),
	PSERVER_NEW_SLOT(item(COMMAND)),
	PSERVER_SLOT(item(STAINED_GLASS_PANE).setDurability(5)),
	WORLD_PSERVER(item(GRASS)),
	GAME_PSERVER(item(DIAMOND_SWORD)),
	GAMESERVER_WOOLBATTLE(item(BOW)),
	PSERVER_DELETE(item(BARRIER)),
	CONFIRM(item(INK_SACK).setDurability(10)),
	CANCEL(item(INK_SACK).setDurability(1)),
	START_PSERVER(item(INK_SACK).setDurability(2)),

	LOADING(item(BARRIER)),

	;

	private final ItemBuilder builder;

	private Item(ItemBuilder builder) {
		this.builder = builder;
	}

	public ItemBuilder getBuilder() {
		return new ItemBuilder(builder);
	}

	public String getDisplayName(User user) {
		return getDisplayName(user, new String[0]);
	}

	public String getDisplayName(User user, String... replacements) {
		return getDisplayName(this, user, replacements);
	}

	public ItemStack getItem(User user) {
		return getItem(this, user);
	}

	public ItemStack getItem(User user, String... replacements) {
		return getItem(this, user, replacements);
	}

	public ItemStack getItem(User user, String[] replacements, String... loreReplacements) {
		return getItem(this, user, replacements, loreReplacements);
	}

	public String getItemId() {
		return getItemId(this);
	}

	public static int countItems(ItemStack item, Inventory inv) {
		int i = 1;
		for (; inv.contains(item, i); i++) {
		}
		if (inv instanceof PlayerInventory) {
			PlayerInventory t = (PlayerInventory) inv;
			List<ItemStack> items = new ArrayList<>();
			items.add(t.getHolder().getItemOnCursor());
			items.add(t.getBoots());
			items.add(t.getChestplate());
			items.add(t.getLeggings());
			items.add(t.getHelmet());
			for (ItemStack s : items) {
				if (item.equals(s)) {
					i += s.getAmount();
				}
			}
		}
		return i - 1;
	}

	public static void removeItems(Inventory invToRemoveFrom, ItemStack itemToRemove, int count) {
		for (int i = 0; i < count; i++)
			invToRemoveFrom.removeItem(itemToRemove);
	}

	public static int countItems(Material item, Inventory inv) {
		int i = 1;
		for (; inv.contains(item, i); i++) {
		}
		if (inv instanceof PlayerInventory) {
			PlayerInventory t = (PlayerInventory) inv;
			List<ItemStack> items = new ArrayList<>();
			items.add(t.getHolder().getItemOnCursor());
			items.add(t.getBoots());
			items.add(t.getChestplate());
			items.add(t.getLeggings());
			items.add(t.getHelmet());
			for (ItemStack s : items) {
				if (s != null)
					if (s.getType() == item) {
						i += s.getAmount();
					}
			}
		}
		return i - 1;
	}

	public static ItemStack getItem(Item item, User user, String... replacements) {
		return getItem(item, user, replacements, new String[0]);
	}

	public static ItemStack getItem(Item item, User user, String[] replacements, String... loreReplacements) {
		ItemBuilder builder = item.getBuilder().getUnsafe().setString("itemId", getItemId(item)).builder();
		String name = getDisplayName(item, user, replacements);
		builder.setDisplayName(name);
		if (builder.getLores().size() != 0) {
			builder.getLores().clear();
			String last = null;
			for (String lore : Message
					.getMessage(Message.PREFIX_ITEM + Message.PREFIX_LORE + item.name(), user.getLanguage(),
							loreReplacements)
					.split("\\%n")) {
				if (last != null) {
					lore = ChatColor.getLastColors(last) + lore;
				}
				last = lore;
				builder.addLore(last);
			}
		}
		return builder.build();
	}

	public static String getItemId(Item item) {
		return Message.PREFIX_ITEM + item.name();
	}

	public static String getLanguageId(ItemStack item) {
		return getNBTValue(new ItemBuilder(item), "language");
	}

	public static String getItemId(ItemStack item) {
		return getNBTValue(new ItemBuilder(item), "itemId");
	}

	public static ItemBuilder setItemId(ItemBuilder b, String itemId) {
		return b.getUnsafe().setString("itemId", itemId).builder();
	}

	private static String getNBTValue(ItemBuilder builder, String key) {
		return builder.getUnsafe().getString(key);
	}

	public static String getDisplayName(Item item, User user, String... replacements) {
		return Message.getMessage(getItemId(item), user.getLanguage(), replacements);
	}

	public static Item byGadget(Gadget gadget) {
		for (Item item : values()) {
			if (item.name().startsWith("GADGET_") && item.name().substring(7).equals(gadget.name())) {
				return item;
			}
		}
		return null;
	}

}
