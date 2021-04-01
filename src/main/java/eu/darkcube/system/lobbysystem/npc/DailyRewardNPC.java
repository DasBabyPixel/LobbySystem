package eu.darkcube.system.lobbysystem.npc;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import eu.darkcube.system.lobbysystem.Lobby;
import eu.darkcube.system.npcapi.DefaultNPC;
import eu.darkcube.system.npcapi.Emote;
import eu.darkcube.system.npcapi.Skin;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

public class DailyRewardNPC extends DefaultNPC {

//	private NPC instance;

	public DailyRewardNPC() {
		super(Lobby.getInstance(), Lobby.getInstance().getDataManager().getDailyRewardNPCLocation(), true,
				"§aDaily Reward", true, null, false, null);
//		instance = this;
		try {
			Field knock = DefaultNPC.class.getDeclaredField("onKnockBack");
			knock.setAccessible(true);
			knock.set(this, new Consumer());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		setSkin(new Skin() {
			@Override
			public String getValue() {
				return "ewogICJ0aW1lc3RhbXAiIDogMTU5MjIzMDU2MTM4NywKICAicHJvZmlsZUlkIiA6ICI5ZDFhNGMwNGQ3M2Y0Njc0ODg0NjBj"
						+ "YjUyYjU5YmEzNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaWVzZXNNTGVubkciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgO"
						+ "iB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy"
						+ "5taW5lY3JhZnQubmV0L3RleHR1cmUvNmU0NGNjNjQ3MjI0MmMyOTcxZTkyZjg2MmVmZjI1ZDg0YWFiYzY1N2E4ZGYyYzB"
						+ "mNmNmZjhlNmQ5ZjFmNmE1IgogICAgfQogIH0KfQ==";
			}

			@Override
			public String getSignature() {
				return "VIIeMriekC/89hPWzsO3mg3spiehxXe9md7fMys1NlNX9PI354gzHMx+Pu1rq8AwBQMxAL6KZumKF3ZlBMTSxiYF3yoj/ddAOeu"
						+ "hUZ0d8n8TaHLk93XFZwu/D5fbvIVXYdCvqHP/STdAgg/gqBGpmINxRH7T+LynzjIc2zM7c7knC0S+3kn7wqo1Ql7s3wOimGH"
						+ "tW4Peqg6EiUTsZZ12Orr9VlVziUyWU1wfrblTROT4GWOXkJ/koQk804P1R0GDpumuY4KSY2hOuUX0ZWYC+6LLPvbiD9+rO7q"
						+ "KoRY8wE0wjbp61yGJ/0/cMeMdyRzi2u7FI7COys7yB9FTqkDPD1XKYECnmqGNUhTSP3u7RHA4TsCZxh418oiZX62k/NUGNBq"
						+ "9a++rWYLgSoabL0G7nDhxwfyf5NyhgPuSUTImKVnvE+M3LGwvO4DH6o/9hjoAFxEZWmx0bfhjZ2VbYQj7etwJeJ+8yBlFbBS"
						+ "mCLqxI5KFgKbnNi4QpR8rioF/HVGr6Nwisb8KigC5Y1ZJpOw3mmGnkqaaS/y6f2fkPThJRNZPjLyW4XwDkXU1ofhIsSAoj7B"
						+ "vmJM2xWh9Z/Qo+JnmkbvKLvx3jSjggPX6vdi3w+1yMzPlZ2V6g7TwSWTtcbL3hQF3je6mvZOdnfnPRvuZGn4m34bQFkpUBsz"
						+ "8y6lseHk=";
			}
		});
		spawn(null);
	}

	public class Consumer implements java.util.function.Consumer<Player> {

		@Override
		public void accept(Player p) {
			if (p.isFlying())
				return;
			Location loc1 = p.getLocation();
			Location loc2 = DailyRewardNPC.this.getLocation();
			double x = loc1.getX() - loc2.getX();
			double y = loc1.getY() - loc2.getY();
			double z = loc1.getZ() - loc2.getZ();
			p.setVelocity(new Vector(x, Math.abs(y) > 1.7 ? 2 : Math.abs(y) + 0.3, z).normalize().multiply(.7));
//				woolbattleNpc.sendEmote(Emote.);
			PacketPlayOutAnimation animation = new PacketPlayOutAnimation();
			try {
				Field eid = animation.getClass().getDeclaredField("a");
				eid.setAccessible(true);
				eid.set(animation, DailyRewardNPC.this.getEntityId());
				Field animationid = animation.getClass().getDeclaredField("b");
				animationid.setAccessible(true);
				animationid.set(animation, 0);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(animation);
			} catch (Exception ex) {
			}
			DailyRewardNPC.this.getSender().sendEmotePacket(p, Emote.KARATE);
		}
	}
}
