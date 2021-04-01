package eu.darkcube.system.lobbysystem.user;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import eu.darkcube.api.cubes.CubesAPI;
import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.lobbysystem.event.EventGadgetSelect;
import eu.darkcube.system.lobbysystem.gadget.Gadget;
import eu.darkcube.system.lobbysystem.inventory.abstraction.Inventory;
import eu.darkcube.system.lobbysystem.inventory.pserver.PServerDataManager.PServerUserSlots;
import eu.darkcube.system.lobbysystem.util.Language;
import eu.darkcube.system.lobbysystem.util.ParticleEffect;
import eu.darkcube.system.lobbysystem.util.UUIDManager;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public abstract class User {

	protected Language language;
	protected Gadget gadget;
	protected UUID uuid;
	protected Inventory openInventory;
	protected boolean buildMode = false;
	protected boolean sounds = true;
	protected boolean animations = true;
	private long lastDailyReward = 0;
	private PServerUserSlots slots;
	private Set<Integer> rewardSlotsUsed;

	public User(Language language, Gadget gadget, boolean sounds, boolean animations, UUID uuid, long lastDailyReward,
			Inventory openInventory, Set<Integer> rewardSlotsUsed) {
		this.rewardSlotsUsed = rewardSlotsUsed;
		this.lastDailyReward = lastDailyReward;
		this.sounds = sounds;
		this.animations = animations;
		this.language = language;
		this.uuid = uuid;
		this.openInventory = openInventory;
		this.slots = new PServerUserSlots(this);
		setGadget(gadget);
	}

	void unload() {
		slots.save();
		slots = null;
		language = null;
		gadget = null;
		uuid = null;
		openInventory = null;
		buildMode = false;
		sounds = false;
		animations = false;
		lastDailyReward = 0;
		rewardSlotsUsed = null;
	}
	
	public PServerUserSlots getSlots() {
		return slots;
	}

	public Inventory getOpenInventory() {
		return openInventory;
	}

	public boolean openInventoryExecute = true;

	public User setOpenInventory(Inventory openInventory) {
		Player p = UUIDManager.getPlayerByUUID(getUniqueId());
		if (p != null) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (openInventory.getHandle() != null) {
						p.openInventory(openInventory.getHandle());
					}
					User.this.openInventory = openInventory;
					if (openInventory.getHandle() != null) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 100, false, false),
								true);
						p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 100, false, false),
								true);
						if (isAnimations()) {
							openInventory.playAnimation(User.this);
						} else {
							openInventory.skipAnimation(User.this);
						}
					}
				}
			};
			if (!MinecraftServer.getServer().isMainThread()) {
				Bukkit.getScheduler().runTask(Lobby.getInstance(), r);
			} else {
				r.run();
			}
		} else {
			this.openInventory = openInventory;
		}
		return this;
	}

	public Set<Integer> getRewardSlotsUsed() {
		return rewardSlotsUsed;
	}

	public void setBuildMode(boolean buildMode) {
		this.buildMode = buildMode;
	}

	public boolean isBuildMode() {
		return buildMode;
	}

	public boolean isSounds() {
		return sounds;
	}

	public boolean isAnimations() {
		return animations;
	}

	public long getLastDailyReward() {
		return lastDailyReward;
	}

	public void setLastDailyReward(long lastDailyReward) {
		this.lastDailyReward = lastDailyReward;
	}

	public void playSound(Sound sound, float volume, float pitch) {
		if (!isSounds())
			return;
		Player p = UUIDManager.getPlayerByUUID(getUniqueId());
		if (p != null) {
			playSound(p.getLocation(), sound, volume, pitch);
		}
	}

	public void playSound(Location loc, Sound sound, float volume, float pitch) {
		if (!isSounds())
			return;
		Player p = UUIDManager.getPlayerByUUID(getUniqueId());
		if (p != null) {
			p.playSound(loc, sound, volume, pitch);
		}
	}

	public void teleport(Location loc) {
		Player p = UUIDManager.getPlayerByUUID(getUniqueId());
		if (p != null) {
//			new BukkitRunnable() {
//				@SuppressWarnings("unchecked")
//				@Override
//				public void run() {
////					p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 100, false, false), true);
			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 100, false, false), true);
//					p.setGameMode(GameMode.SPECTATOR);
//					PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
//					try {
//						Field a = packet.getClass().getDeclaredField("a");
//						a.setAccessible(true);
//						a.set(packet, EnumPlayerInfoAction.UPDATE_GAME_MODE);
//						Field b = packet.getClass().getDeclaredField("b");
//						b.setAccessible(true);
//						List<PlayerInfoData> data = (List<PlayerInfoData>) b.get(packet);
//						EntityPlayer ep = ((CraftPlayer) p).getHandle();
//
//						data.add(packet.new PlayerInfoData(ep.getProfile(), ep.ping, EnumGamemode.SURVIVAL,
//								ep.getPlayerListName()));
//					} catch (Exception ex) {
//					}
//					for (Player o : Bukkit.getOnlinePlayers()) {
//						if (o.canSee(p)) {
//							((CraftPlayer) o).getHandle().playerConnection.sendPacket(packet);
//						}
//					}
//				}
//			}.runTask(Lobby.getInstance());
//			playSound(Sound.GHAST_FIREBALL, 10, 1);
//			BukkitRunnable r = new BukkitRunnable() {
//				@Override
//				public void run() {
//					p.teleport(p.getLocation().add(0, 3, 0));
//				}
//			};
//			r.runTaskTimer(Lobby.getInstance(), 1, 1);
//			new BukkitRunnable() {
//				@Override
//				public void run() {
//					r.cancel();
			playSound(Sound.NOTE_PLING, 10, 1);
			p.teleport(loc);
			p.setGameMode(GameMode.SURVIVAL);
			p.setAllowFlight(true);
//					p.removePotionEffect(PotionEffectType.BLINDNESS);

//				}
//			}.runTaskLater(Lobby.getInstance(), 8);
			new BukkitRunnable() {
				double t = 0;
				double r = .8;
//				Location loc = p.getLocation();
				double x, y, z;

				@Override
				public void run() {
					for (int i = 0; i < 4; i++) {
						t = t + Math.PI / 16;
						x = r * Math.cos(t);
						y = 0.1 * t;
						z = r * Math.sin(t);
						loc.add(x, y, z);
						ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, loc, 20);
						loc.subtract(x, y, z);
					}
					if (t > Math.PI * 8) {
						this.cancel();
					}
				}
			}.runTaskTimer(Lobby.getInstance(), 1, 1);
		}
	}

	public void setSounds(boolean sounds) {
		this.sounds = sounds;
	}

	public void setAnimations(boolean animations) {
		this.animations = animations;
	}

	public UserData newUserData() {
		return new UserData(language, gadget, sounds, animations, lastDailyReward, rewardSlotsUsed);
	}

	public User setGadget(Gadget gadget) {
		EventGadgetSelect e = new EventGadgetSelect(this, gadget);
		Bukkit.getPluginManager().callEvent(e);
		if (!e.isCancelled())
			this.gadget = gadget;
		return this;
	}

	public Gadget getGadget() {
		return gadget;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public BigInteger getCubes() {
		return CubesAPI.getCubes(uuid);
	}

	public void setCubes(BigInteger cubes) {
		CubesAPI.setCubes(uuid, cubes);
	}

	public Language getLanguage() {
		return language;
	}

	public User setLanguage(Language language) {
		this.language = language;
		return this;
	}

	public User save() {
		return UserWrapper.saveUser(this);
	}
}
