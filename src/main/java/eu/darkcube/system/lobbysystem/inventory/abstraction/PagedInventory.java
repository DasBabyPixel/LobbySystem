package eu.darkcube.system.lobbysystem.inventory.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.UUIDManager;

public abstract class PagedInventory extends Inventory {

	protected Item mainItem;
	protected int[] TOTAL_SLOTS;
	protected int[] TOTAL_SORT;
	protected int[] SLOTS;
	protected int[] SORT;
	protected int maxSort;
	protected int maxTotalSort;
	protected Set<User> open = new HashSet<>();
	protected Map<User, Integer> page = new HashMap<>();
	protected Map<User, InventoryManager> managers = new HashMap<>();

	public PagedInventory(String title, Item mainItem, int size, InventoryType type, int... pageSlots) {
		super(Bukkit.createInventory(null, size, title), type);
		if (size % 9 != 0) {
			throw new IllegalArgumentException("Illegal size: " + size);
		}
		this.mainItem = mainItem;

		int start = s(1, 5);

		SLOTS = new int[pageSlots.length];
		SORT = new int[SLOTS.length];
		for (int i = 0; i < SLOTS.length; i++) {
			SLOTS[i] = pageSlots[i];
			SORT[i] = dist(start, SLOTS[i]);
		}
		List<Integer> l = new ArrayList<>();
		for (int s : SORT)
			l.add(s);
		maxSort = l.stream().mapToInt(i -> i).max().orElse(0);

		TOTAL_SLOTS = new int[size];
		TOTAL_SORT = new int[TOTAL_SLOTS.length];
		int[] allslots = box(1, 1, size / 9, 9);
		for (int i = 0; i < TOTAL_SLOTS.length; i++) {
			TOTAL_SLOTS[i] = allslots[i];
			TOTAL_SORT[i] = dist(start, TOTAL_SLOTS[i]);
		}
		l = new ArrayList<>();
		for (int s : TOTAL_SORT)
			l.add(s);
		maxTotalSort = l.stream().mapToInt(i -> i).max().orElse(0);

	}

	public PagedInventory(String title, Item mainItem, int size, InventoryType type) {
		this(title, mainItem, size, type, box(3, 2, 5, 8));
	}

	public PagedInventory(String title, int size, InventoryType type) {
		this(title, null, size, type);
	}

	public int getPages(User user) {
		Map<Integer, ItemStack> contents = getContents(user);
		int max = contents.keySet().stream().mapToInt(i -> i).max().orElse(-1);
		if (max == -1)
			return 1;
		return (max) / SLOTS.length + 1;
	}

	public void setPage(User user, int page) {
		if (!hasOpened(user)) {
			return;
		}
		if (page < 1) {
			page = 1;
		}
		int pages = getPages(user);
		if (page > pages) {
			page = pages;
		}
		this.page.put(user, page);
		update(user);
	}

	public boolean hasOpened(User user) {
		return open.contains(user);
	}

	public int getPage(User user) {
		return page.getOrDefault(user, 1);
	}

	public int getPageSize() {
		return SLOTS.length;
	}

	public int getTotalPageSize() {
		return TOTAL_SLOTS.length;
	}

	protected void update(User user) {
		getManager(user).ifPresent(InventoryManager::update);
	}

	private void load(User user, boolean animation) {
		InventoryManager m = new InventoryManager(user, animation);
		getManager(user).ifPresent(InventoryManager::cancel);
		managers.put(user, m);
		m.runTaskTimer(Lobby.getInstance(), 1, 1);
	}

	private Optional<InventoryManager> getManager(User user) {
		return Optional.ofNullable(managers.get(user));
	}

	@Override
	public void playAnimation(User user) {
		load(user, true);
	}

	@Override
	public void skipAnimation(User user) {
		load(user, false);
	}

	protected abstract void onOpen(User user);

	protected abstract void onClose(User user);

	protected abstract Map<Integer, ItemStack> getContents(User user);

	protected abstract Map<Integer, ItemStack> getStaticContents(User user);

	protected void insertDefaultItems(@SuppressWarnings("unused") InventoryManager manager) {

	}

	public class InventoryManager extends BukkitRunnable {
		public boolean playSound = true;
		public final User user;
		public boolean animate = false;
		public boolean updateRequired = false;
		public int ticksOpen = -2;
		public Set<ArrowType> enabled = new HashSet<>();
		public Map<ArrowType, Integer[]> arrowSlots = new HashMap<>();
		public Map<Integer, ItemStack> fallbackItems = new HashMap<>();
		public Map<Integer, ItemStack> staticItems = new HashMap<>();
		public Map<Integer, ItemStack> pageItems = new HashMap<>();
		public Runnable updater;
		public boolean updaterUpdate = false;
		public boolean updaterUpdated = false;

		private InventoryManager(User user, boolean animate) {
			this.user = user;
			this.animate = animate;

			updater = new Runnable() {

				@Override
				public void run() {
					if (updaterUpdate) {
						updaterUpdate = false;
					} else {
						return;
					}

					calc();

					updaterUpdated = true;
				}

				private void calc() {
					int pages = getPages(user);
					int page = getPage(user);
					if (page > pages) {
						page = pages;
						setPage(user, page);
					} else if (page < 1) {
						page = 1;
					}
					if (page == 1) {
						hideArrow(ArrowType.PREV);
					} else {
						showArrow(ArrowType.PREV);
					}
					if (page >= pages) {
						hideArrow(ArrowType.NEXT);
					} else {
						showArrow(ArrowType.NEXT);
					}
					Set<Integer> pagedSlots = new HashSet<>();
					for (int slot : SLOTS) {
						pagedSlots.add(slot);
					}

					final int skip = (page - 1) * SLOTS.length;

					Map<Integer, ItemStack> contents = getContents(user);
					Map<Integer, ItemStack> staticContents = getStaticContents(user);

					for (ArrowType type : arrowSlots.keySet()) {
						if (!enabled.contains(type)) {
							String l = Item.getItemId(getLeftArrowItem());
							String r = Item.getItemId(getRightArrowItem());
							for (int slot : arrowSlots.get(type)) {
								if (staticItems.containsKey(slot)) {
									ItemStack i = staticItems.get(slot);
									String itemid = Item.getItemId(i);
									if (itemid.equals(type == ArrowType.PREV ? l : r)) {
										staticItems.remove(slot);
									}
								}
							}
							continue;
						}
						for (int slot : arrowSlots.get(type)) {
							if (!staticItems.containsKey(slot)) {
								staticItems.put(slot,
										type == ArrowType.PREV ? getLeftArrowItem() : getRightArrowItem());
							}
						}
					}

					pageItems.clear();
					for (int slotId : contents.keySet()) {
						ItemStack item = contents.get(slotId);
						slotId -= skip;
						if (slotId >= SLOTS.length || slotId < 0) {
							continue;
						}
						int slot = SLOTS[slotId];
						pageItems.put(slot, item);
					}

					staticItems.clear();
					for (int slotId : staticContents.keySet()) {
						ItemStack item = staticContents.get(slotId);
						slotId -= skip;
						if (slotId >= TOTAL_SLOTS.length || slotId < 0) {
							continue;
						}
						int slot = TOTAL_SLOTS[slotId];
						staticItems.put(slot, item);
					}

				}
			};
//			updater.runTaskTimer(Lobby.getInstance(), 1, 1);
		}

		@Override
		public synchronized void cancel() throws IllegalStateException {
			onClose(user);

//			updater.cancel();
			updater = null;

			enabled.clear();
			enabled = null;
			arrowSlots.clear();
			arrowSlots = null;
			fallbackItems.clear();
			fallbackItems = null;
			staticItems.clear();
			staticItems = null;
			pageItems.clear();
			pageItems = null;

			ticksOpen = 0;

			page.remove(user);
			managers.remove(user);
			open.remove(user);
			super.cancel();
		}

		@Override
		public void run() {
			if (user.getOpenInventory() != PagedInventory.this && ticksOpen > 0) {
				cancel();
				return;
			}
			if (ticksOpen == 0) {
				open.add(user);
				onOpen(user);
				insertDefaultItems(this);
				updateInSync();
				if (!animate && playSound) {
					user.playSound(Sound.NOTE_STICKS, 1, 1);
				}
			}
			ticksOpen++;
			if (updateRequired) {
				updateRequired = false;
				updaterUpdated = false;
				updaterUpdate = true;
				updater.run();
//				updateInSync();
			}
			if (updaterUpdated) {
				updaterUpdated = false;
				updateInSync();
			} else if (ticksOpen <= maxTotalSort) {
				updateInSync();
				if (animate && playSound) {
					user.playSound(Sound.NOTE_STICKS, 1, 1);
				}
			}
		}

		public void update() {
			updateRequired = true;
		}

		public void updateInSync() {
			Player p = UUIDManager.getPlayerByUUID(user.getUniqueId());
			for (int slot = 0; slot < handle.getSize(); slot++) {
				handle.setItem(slot, getItem(slot));
			}
			p.updateInventory();
		}

		public void setStaticItem(int slot, ItemStack item) {
			if (item != null) {
				staticItems.put(slot, item);
			} else if (staticItems.containsKey(slot)) {
				staticItems.remove(slot);
			} else {
				return;
			}
			update();
		}

		public void setPageItem(int slot, ItemStack item) {
			if (item != null) {
				pageItems.put(slot, item);
			} else if (pageItems.containsKey(slot)) {
				pageItems.remove(slot);
			} else {
				return;
			}
			update();
		}

		public void setFallbackItem(int slot, ItemStack item) {
			if (item != null) {
				fallbackItems.put(slot, item);
			} else if (fallbackItems.containsKey(slot)) {
				fallbackItems.remove(slot);
			} else {
				return;
			}
			update();
		}

		public void showArrow(ArrowType type) {
			enabled.add(type);
		}

		public void hideArrow(ArrowType type) {
			enabled.remove(type);
		}

		public ItemStack getLeftArrowItem() {
			return Item.PREV.getItem(user);
		}

		public ItemStack getRightArrowItem() {
			return Item.NEXT.getItem(user);
		}

		public ItemStack getItem(int slot) {
			int sort = TOTAL_SORT[slot];
			if (animate && sort > ticksOpen) {
				return null;
			}
			return getItemUnsafe(slot);
		}

		public ItemStack getItemUnsafe(int slot) {
			if (Arrays.asList(arrowSlots.getOrDefault(ArrowType.PREV, new Integer[0])).contains(slot)
					&& enabled.contains(ArrowType.PREV)) {
				return getLeftArrowItem();
			}
			if (Arrays.asList(arrowSlots.getOrDefault(ArrowType.NEXT, new Integer[0])).contains(slot)
					&& enabled.contains(ArrowType.NEXT)) {
				return getRightArrowItem();
			}
			ItemStack i = null;
			if (fallbackItems.containsKey(slot)) {
				i = fallbackItems.get(slot);
			}
			if (staticItems.containsKey(slot)) {
				i = staticItems.get(slot);
			}
			if (pageItems.containsKey(slot)) {
				i = pageItems.get(slot);
			}
			return i;
		}
	}

	public static enum ArrowType {
		PREV, NEXT;
	}

	protected static Map<Integer, ItemStack> convert(Map<Integer, Item> map, User user) {
		Map<Integer, ItemStack> m = new HashMap<>();
		for (int k : map.keySet()) {
			m.put(k, map.get(k).getItem(user));
		}
		return m;
	}

	protected static int[] box(int r1, int i1, int r2, int i2) {
		final int fr1 = Math.min(r1, r2);
		final int fr2 = Math.max(r1, r2);
		final int fi1 = Math.min(i1, i2);
		final int fi2 = Math.max(i1, i2);
		int[] res = new int[(fr2 - fr1 + 1) * (fi2 - fi1 + 1)];
		int index = 0;
		for (int r = fr1; r <= fr2; r++) {
			for (int i = fi1; i <= fi2; i++, index++) {
				res[index] = s(r, i);
			}
		}
		return res;
	}
}
