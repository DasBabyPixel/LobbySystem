package eu.darkcube.system.lobbysystem;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceLifeCycle;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import eu.darkcube.system.GameState;
import eu.darkcube.system.Plugin;
import eu.darkcube.system.commandapi.CommandAPI;
import eu.darkcube.system.loader.ReflectionClassLoader;
import eu.darkcube.system.lobbysystem.command.CommandLobbysystem;
import eu.darkcube.system.lobbysystem.command.lobbysystem.CommandBuild;
import eu.darkcube.system.lobbysystem.gadget.listener.ListenerGrapplingHook;
import eu.darkcube.system.lobbysystem.gadget.listener.ListenerHookArrow;
import eu.darkcube.system.lobbysystem.listener.*;
import eu.darkcube.system.lobbysystem.npc.DailyRewardNPC;
import eu.darkcube.system.lobbysystem.npc.WoolBattleNPC;
import eu.darkcube.system.lobbysystem.pserver.PServerSupport;
import eu.darkcube.system.lobbysystem.user.User;
import eu.darkcube.system.lobbysystem.user.UserWrapper;
import eu.darkcube.system.lobbysystem.util.DataManager;
import eu.darkcube.system.lobbysystem.util.DependencyManager;
import eu.darkcube.system.lobbysystem.util.Item;
import eu.darkcube.system.lobbysystem.util.Language;
import eu.darkcube.system.lobbysystem.util.Message;
import eu.darkcube.system.lobbysystem.util.SkullCache;
import eu.darkcube.system.lobbysystem.util.UUIDManager;
import eu.darkcube.system.npcapi.NPC;

public class Lobby extends Plugin {

	private static Lobby instance;
	private DataManager dataManager;
	private NPC woolbattleNpc;
	private NPC dailyRewardNpc;

	public Lobby() {
		instance = this;
	}

	@Override
	public void onLoad() {
		new DependencyManager(new ReflectionClassLoader(this)).loadDependencies();
	}

	@Override
	public void onEnable() {
		PServerSupport.init();
		// Load all messages
		for (Language language : Language.values()) {
			if (language.getBundle() != null) {
				for (Message message : Message.values()) {
					if (message.getMessage(language).startsWith(message.name())) {
						sendConsole("§cCould not load message " + message.name() + " in language " + language.name());
					}
				}
			} else {
				sendConsole("§cLanguage bundle for language " + language.name() + " could not be found!");
			}
			for (Item item : Item.values()) {
				String id = item.getItemId();
				try {
					language.getBundle().getString(id);
				} catch (Exception ex) {
					sendConsole("§cCould not load item name " + id + " in language " + language.name());
				}
				if (item.getBuilder().getLores().size() != 0) {
					id = item.name();
					try {
						language.getBundle().getString(Message.PREFIX_ITEM + Message.PREFIX_LORE + id);
					} catch (Exception ex) {
						sendConsole("§cCould not load item lore " + Message.PREFIX_ITEM + Message.PREFIX_LORE + id
								+ " in language " + language.name());
					}
				}
			}
		}

//		LobbyPermission.registerAll();

		dataManager = new DataManager();
		woolbattleNpc = new WoolBattleNPC();
		dailyRewardNpc = new DailyRewardNPC();

		CommandAPI.enable(this, new CommandLobbysystem());
		CommandAPI.enable(this, new CommandBuild());

		for (World world : Bukkit.getWorlds()) {
			world.setGameRuleValue("randomTickSpeed", "0");
			world.setGameRuleValue("doDaylightCycle", "false");
			world.setFullTime(6000);
		}

		new BukkitRunnable() {
			private final Collection<String> woolbattleTasks = getDataManager().getWoolBattleTasks();

			@Override
			public void run() {
				for (String task : woolbattleTasks) {
					Collection<ServiceInfoSnapshot> services = CloudNetDriver.getInstance().getCloudServiceProvider()
							.getCloudServices(task);
					int freeServices = 0;
					for (ServiceInfoSnapshot service : services) {
						GameState state =
								GameState.fromString(service.getProperty(BridgeServiceProperty.STATE).orElse(null));
						if (state == GameState.LOBBY || state == GameState.UNKNOWN) {
							freeServices++;
						}
					}
					if (CloudNetDriver.getInstance().getServiceTaskProvider().isServiceTaskPresent(task)) {
						ServiceTask serviceTask = CloudNetDriver.getInstance().getServiceTaskProvider().getServiceTask(task);
						if (freeServices < serviceTask.getMinServiceCount()) {
							ServiceInfoSnapshot snap =
									CloudNetDriver.getInstance().getCloudServiceFactory().createCloudService(serviceTask);
							CloudNetDriver.getInstance().getCloudServiceProvider(snap)
									.setCloudServiceLifeCycle(ServiceLifeCycle.RUNNING);
						}
					} else {
						sendMessage("§cCould not find service task named " + task);
					}
				}
			}
		}.runTaskTimerAsynchronously(this, 30 * 20, 30 * 20);

		for (Player p : Bukkit.getOnlinePlayers()) {
			UserWrapper.loadUser(p.getUniqueId());
		}

		new ListenerSettingsJoin();
		new ListenerScoreboard();
		new ListenerQuit();
		new ListenerBlock();
		new ListenerDoublejump();
		new ListenerDamage();
		new ListenerInventoryClick();
		new ListenerInventoryClose();
		new ListenerInteract();
		new ListenerLobbySwitcher();
		new ListenerWoolBattleNPC();
		new ListenerDailyRewardNPC();
		new ListenerMinigameServer();
		new ListenerItemDropPickup();
		new ListenerFish();
		new ListenerGadget();
		new ListenerDailyReward();
		new ListenerHookArrow();
		new ListenerGrapplingHook();
		new ListenerBoostPlate();
		new ListenerWeather();
		new ListenerPServer();
		new ListenerPhysics();

		for (Player p : Bukkit.getOnlinePlayers()) {
			ListenerSettingsJoin.instance.handle(new PlayerJoinEvent(p, "Custom"));
			ListenerScoreboard.instance.handle(new PlayerJoinEvent(p, "Custom"));
			User user = UserWrapper.getUser(p.getUniqueId());
			user.setGadget(user.getGadget());
		}
		new ListenerBorder();
		
		SkullCache.register();

	}

	@Override
	public void onDisable() {
		SkullCache.unregister();
		for (User user : new HashSet<>(UserWrapper.users.values())) {
			getDataManager().setUserPos(user.getUniqueId(),
					UUIDManager.getPlayerByUUID(user.getUniqueId()).getLocation());
			UserWrapper.unloadUser(user);
		}
	}

	public NPC getWoolBattleNPC() {
		return woolbattleNpc;
	}

	public NPC getDailyRewardNpc() {
		return dailyRewardNpc;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	@Override
	public String getCommandPrefix() {
		return "§aLobbySystem";
	}

	public static Lobby getInstance() {
		return instance;
	}
}
