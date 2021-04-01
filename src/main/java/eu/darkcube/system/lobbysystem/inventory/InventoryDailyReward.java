package eu.darkcube.system.lobbysystem.inventory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.inventory.abstraction.Inventory;
import eu.darkcube.system.lobbysystem.inventory.abstraction.InventoryType;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.ItemBuilder;
import eu.darkcube.system.lobbysystem.util.Message;

public class InventoryDailyReward extends Inventory {

	public InventoryDailyReward(User user) {
		super(Bukkit.createInventory(null, 5 * 9, Message.INVENTORY_NAME_DAILY_REWARD.getMessage(user)),
				InventoryType.DAILY_REWARD);
	}

	@Override
	public void playAnimation(User user) {
		animate(user, false);
	}

	@Override
	public void skipAnimation(User user) {
		animate(user, true);
	}

	public void setItems(User user) {
		ItemStack used = new ItemBuilder(Material.SULPHUR).setDisplayName(Message.REWARD_ALREADY_USED.getMessage(user))
				.build();
		ItemStack unused = new ItemBuilder(Material.GLOWSTONE_DUST).setDisplayName("§e???").build();
		Set<Integer> usedSlots = user.getRewardSlotsUsed();
		Map<Integer, ItemStack> items = new HashMap<>();
		if (usedSlots.contains(1)) {
			used = new ItemBuilder(used).getUnsafe().setInt("reward", 1).builder().getItemStack();
			items.put(21, used);
		} else {
			unused = new ItemBuilder(unused).getUnsafe().setInt("reward", 1).builder().getItemStack();
			items.put(21, unused);
		}
		if (usedSlots.contains(2)) {
			used = new ItemBuilder(used).getUnsafe().setInt("reward", 2).builder().getItemStack();
			items.put(22, used);
		} else {
			unused = new ItemBuilder(unused).getUnsafe().setInt("reward", 2).builder().getItemStack();
			items.put(22, unused);
		}
		if (usedSlots.contains(3)) {
			used = new ItemBuilder(used).getUnsafe().setInt("reward", 3).builder().getItemStack();
			items.put(23, used);
		} else {
			unused = new ItemBuilder(unused).getUnsafe().setInt("reward", 3).builder().getItemStack();
			items.put(23, unused);
		}
		for (int slot : items.keySet()) {
			handle.setItem(slot, items.get(slot));
		}
	}

	private void animate(User user, boolean instant) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(user.getLastDailyReward());
		Calendar c2 = Calendar.getInstance();
//		c2.setTimeInMillis(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		if (c.get(Calendar.DAY_OF_YEAR) != c2.get(Calendar.DAY_OF_YEAR)) {
			user.getRewardSlotsUsed().clear();
		}

		new BukkitRunnable() {
			private ItemStack i1 = Item.LIGHT_GRAY_GLASS_PANE.getItem(user);
			private ItemStack i2 = Item.DARK_GRAY_GLASS_PANE.getItem(user);
			private int count = 0;

			@Override
			public void run() {
				if (count >= 20) {
					this.cancel();
					return;
				}
				switch (count) {
				case 0:
					handle.setItem(4, i1);
					handle.setItem(3, i2);
					handle.setItem(5, i2);
					break;
				case 1:
					handle.setItem(2, i1);
					handle.setItem(6, i1);
					break;
				case 2:
					handle.setItem(1, i2);
					handle.setItem(7, i2);
					break;
				case 3:
					handle.setItem(0, i1);
					handle.setItem(8, i1);
					break;
				case 4:
					handle.setItem(9, i2);
					handle.setItem(17, i2);
					break;
				case 5:
					handle.setItem(18, i1);
					handle.setItem(26, i1);
					break;
				case 6:
					handle.setItem(27, i2);
					handle.setItem(35, i2);
					break;
				case 7:
					handle.setItem(36, i1);
					handle.setItem(44, i1);
					break;
				case 8:
					handle.setItem(37, i2);
					handle.setItem(43, i2);
					break;
				case 9:
					handle.setItem(38, i1);
					handle.setItem(42, i1);
					break;
				case 10:
					handle.setItem(39, i2);
					handle.setItem(41, i2);
					break;
				case 11:
					handle.setItem(40, i1);
					break;
				case 15:
					user.playSound(Sound.LEVEL_UP, 1, 1);
					setItems(user);
					break;
				default:
					break;
				}

				count++;
				if (instant)
					run();
			}
		}.runTaskTimer(Lobby.getInstance(), 1, 1);
	}
}
