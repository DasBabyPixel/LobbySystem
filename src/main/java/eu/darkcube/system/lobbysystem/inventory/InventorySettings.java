package eu.darkcube.system.lobbysystem.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.abstraction.Inventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.lobbysystem.util.SkullUtils;

public class InventorySettings extends Inventory {

	private User user;

	public InventorySettings(User user) {
		super(Bukkit.createInventory(null, 6 * 9, Item.INVENTORY_SETTINGS.getDisplayName(user)),
				InventoryType.SETTINGS);
		this.user = user;
	}

	@Override
	public void playAnimation(User user) {
		animate(user == null ? this.user : user, false);
	}

	@Override
	public void skipAnimation(User user) {
		animate(user == null ? this.user : user, true);
	}

	private void animate(User user, boolean instant) {
		user.playSound(Sound.NOTE_STICKS, 1, 1);
		new BukkitRunnable() {
			private int count = 0;
			private final ItemStack i1 = Item.LIGHT_GRAY_GLASS_PANE.getItem(user);
			private final ItemStack i2 = Item.DARK_GRAY_GLASS_PANE.getItem(user);

			@Override
			public void run() {
				if (count >= 13 || user.getOpenInventory() != InventorySettings.this) {
					this.cancel();
					return;
				}
				if (!instant)
					user.playSound(Sound.NOTE_STICKS, 1, 1);
				switch (count) {
				case 0:
					handle.setItem(0, i1);
					handle.setItem(1, i1);
					handle.setItem(9, i2);
					break;
				case 1:
					handle.setItem(2, i1);
					handle.setItem(10, i2);
					handle.setItem(18, i1);
					break;
				case 2:
					handle.setItem(3, i1);
					handle.setItem(11, i2);
					handle.setItem(19, i2);
					handle.setItem(27, i2);
					break;
				case 3:
					handle.setItem(4, Item.INVENTORY_SETTINGS.getItem(user));
					handle.setItem(12, i1);
					handle.setItem(20, i2);
					handle.setItem(28, i1);
					handle.setItem(36, i1);
					break;
				case 4:
					handle.setItem(5, i1);
					handle.setItem(13, i1);
					handle.setItem(21, i2);
					if (user.isSounds()) {
						handle.setItem(29, Item.INVENTORY_SETTINGS_SOUNDS_ON.getItem(user));
					} else {
						handle.setItem(29, Item.INVENTORY_SETTINGS_SOUNDS_OFF.getItem(user));
					}
					handle.setItem(37, i2);
					handle.setItem(45, i1);
					break;
				case 5:
					handle.setItem(6, i1);
					handle.setItem(14, i1);
					handle.setItem(22, i2);
					handle.setItem(30, i1);
					handle.setItem(38, i1);
					handle.setItem(46, i1);
					break;
				case 6:
					handle.setItem(7, i1);
					handle.setItem(15, i2);
					handle.setItem(23, i2);
					String textureId = "";
					String name = "";
					switch (user.getLanguage()) {
					case ENGLISH:
						textureId = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1"
								+ "cmUvYTE3MDFmMjE4MzVhODk4YjIwNzU5ZmIzMGE1ODNhMzhiOTk0YWJmNjBkMzkxMmFiNGNlOWYyMzExZTc0Zj"
								+ "cyIn19fQ==";
						name = "English";
						break;
					case GERMAN:
						textureId = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1"
								+ "cmUvNWU3ODk5YjQ4MDY4NTg2OTdlMjgzZjA4NGQ5MTczZmU0ODc4ODY0NTM3NzQ2MjZiMjRiZDhjZmVjYzc3Yj"
								+ "NmIn19fQ==";
						name = "Deutsch";
						break;
					}
					ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName("§e" + name);
					item.setItemMeta(meta);
					item = new ItemBuilder(item).getUnsafe().setString("language", user.getLanguage().name()).builder()
							.getItemStack();
					SkullUtils.setSkullTextureId(item, textureId);
					handle.setItem(31, item);
//					handle.setItem(31, Item.INVENTORY_COMPASS_WOOLBATTLE.getItem(user));
					handle.setItem(39, i2);
					handle.setItem(47, i2);
					break;
				case 7:
					handle.setItem(8, i1);
					handle.setItem(16, i2);
					handle.setItem(24, i2);
					handle.setItem(32, i1);
					handle.setItem(40, i1);
					handle.setItem(48, i2);
					break;
				case 8:
					handle.setItem(17, i2);
					handle.setItem(25, i2);
					if (user.isAnimations()) {
						handle.setItem(33, Item.INVENTORY_SETTINGS_ANIMATIONS_ON.getItem(user));
					} else {
						handle.setItem(33, Item.INVENTORY_SETTINGS_ANIMATIONS_OFF.getItem(user));
					}
					handle.setItem(41, i2);
					handle.setItem(49, i2);
					break;
				case 9:
					handle.setItem(26, i1);
					handle.setItem(34, i1);
					handle.setItem(42, i1);
					handle.setItem(50, i2);
					break;
				case 10:
					handle.setItem(35, i2);
					handle.setItem(43, i2);
					handle.setItem(51, i2);
					break;
				case 11:
					handle.setItem(44, i1);
					handle.setItem(52, i1);
					break;
				case 12:
					handle.setItem(53, i1);
					break;
				}
				count++;
				if (instant)
					run();
			}
		}.runTaskTimer(Lobby.getInstance(), 1, 1);
	}
}
