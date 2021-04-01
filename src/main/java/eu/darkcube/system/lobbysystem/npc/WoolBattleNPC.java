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

public class WoolBattleNPC extends DefaultNPC {

//	private NPC instance;

	public WoolBattleNPC() {
		super(Lobby.getInstance(), Lobby.getInstance().getDataManager().getWoolBattleNPCLocation(), true,
				"§5Wool§dBattle", true, null, false, null);
//		instance = this;
		try {
			Field knock = DefaultNPC.class.getDeclaredField("onKnockBack");
			knock.setAccessible(true);
			knock.set(this, new Consumer());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		setSkin(Skin.LOBBY_SELECTOR);
//		setSkin(new Skin() {
//
//			@Override
//			public String getValue() {
//				return "ewogICJ0aW1lc3RhbXAiIDogMTU5MTYyNjcyMjQ2MywKICAicHJvZmlsZUlkIiA6ICJiMGM5MjQ2ZTcyMDE0ZDI2YTc3NWFmNDIyMTZkOTUwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGl0ZURhcmtuZXNzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmNThhMmQ0Y2IyMDIzYWRhNmI4M2Y5NzliOTUyZjFlY2ZjNGI0Y2MwNmY0NzQxMGMwZWI3YjU4NGY0M2Q5YjUiCiAgICB9CiAgfQp9";
//			}
//
//			@Override
//			public String getSignature() {
//				return "klyGMbTVCzsg+YjkMf2uBNdk1dUKOuq4pthi7uybUSlkZmClOfxPwXtWJ1n2fcfROmw5KO+J87zvsqLkdn5ky5yyQudy4texUS3gNxmA8trTM5QnSheUTyZDc/ceg378h2KUqkJiX/idWP/ZhN6ME83xj3/lDJU2Zlt/Fwg9DwPpRKJDJxDuiDLYm9c/JEbIHddy2i3LBIK5v2ePlIoXjLvKu/f+Rg3G/83t9t3qPkjJNcLh+BW9cEyBGTVjDKPAqo4P6CbtEEgaLv/bvt0I7VG+n40TdsHZWX371t4mzh19SJQwus6T4JKoga83o6qB/0AKr8dk2AGMA8Aj2JaGoL1Q7lAWBdyauG1hu0lMO3IvQpEm6hrQI/w64XW8mQoeQRPxJ3dd9PmpYY6voV3CTI3Ol5/XdyXQpuHaUl+yw3YUxKB9YckCkro5GHFvm7WzWa1YMK0soYy+5uXe4esD4XAH0BBIdVyFRWIFZ69H0WFi40rrEZHuZv6LACon61q+uCCtKEvHBmdX5nqK8tmktq6AHrLkSwV/gaQSF7+Bkq3MIBA4YnTMxtaqgkxSpqF9hdcS06EYoTvQpV6kMFxXRifhY6/WfR9dnZyKBsz2jUINxlExenfq5P4OuBynoeUZfARdi8H0GqykYxRwRsyeSUSFYgBaMoP5jiRuK9jtfwI=";
//			}
//		});
		setSkin(new Skin() {
			
			@Override
			public String getValue() {
				return "ewogICJ0aW1lc3RhbXAiIDogMTU5MTYyOTc4Mzc2MSwKICAicHJvZmlsZUlkIiA6ICJiMGM5MjQ2ZTcyMDE0ZDI2YTc3NWFmNDIyMTZkOTUwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGl0ZURhcmtuZXNzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZkMTg3MzkxZmM2ZmNjYzhhMDdmMmI2NGJiZGUyODgwN2NjZTZjOGZiNzA0MDZiY2U2ZTcxNDg2MDc0YzdjOGMiCiAgICB9CiAgfQp9";
			}
			
			@Override
			public String getSignature() {
				return "Bgywd/32NsH1kHtHRSz7pxeXTfaOPjFOzLjAFkV+Vjdq4gqCuoEMhkLo8wVXYk8tmUkz53+6VcQjoQhheJ0kjBXWVbLdGO83apAbjveZ4hOezDatW9Vv8YbD8+9vjEFOs8HuPWB1b6630xZKqcNwQqE+Q4q3hJBRxtlnV/qw8SjsJDKaTA8gLtJ/xl2fa5WNprjIvfULr7RUrJCSfB+a9BZKKcESnQ6ta7id+sGw/j0v/0eBBoq975SW0MSdDTuknn1P64qZTzJfEuIv1etTj2F+Wl6JVrEyamtPFKSA+rltbh64Foz4ebIV0JxIkpjCMO3puwsB2P5NqzvbRLjS7Jr0sEgORGpZD4Gck02Q2+cIQ9wuPkiKGn9sO5fGY97gXB4p/zfVOqty56lUclp7nnuw962bTUD8Mkl1jJQWoKlWOjLNVC9FY9XaTMyaReMH4Xgu6FVMPdEzyrGKndJPD6mkwzWCC+QQaeAK6bgaOMewrdct6NLi0NJch/lQSJ1F9mhZffV2dJrIFl4ul7ngxgw2JPxKNr8vAEFI00HlaLBEm38Zy0A4sj4H/7iYTU+/0i7QQio+/AKJWGcOQw8ZHvfCG1L8GvxYUTp15N/Nn9UNyiBeAvrKEnz6vVTHQ16qJgS0N8wBvYipKIxY6JF483VS+PMsaxtpAi5uhKi4X9s=";
			}
		});
		spawn(null);
	}

	private class Consumer implements java.util.function.Consumer<Player> {

		@Override
		public void accept(Player p) {
			if (p.isFlying())
				return;
			Location loc1 = p.getLocation();
			Location loc2 = WoolBattleNPC.this.getLocation();
			double x = loc1.getX() - loc2.getX();
			double y = loc1.getY() - loc2.getY();
			double z = loc1.getZ() - loc2.getZ();
			p.setVelocity(new Vector(x, Math.abs(y) > 1.7 ? 2 : Math.abs(y) + 0.3, z).normalize().multiply(.7));
//				woolbattleNpc.sendEmote(Emote.);
			PacketPlayOutAnimation animation = new PacketPlayOutAnimation();
			try {
				Field eid = animation.getClass().getDeclaredField("a");
				eid.setAccessible(true);
				eid.set(animation, WoolBattleNPC.this.getEntityId());
				Field animationid = animation.getClass().getDeclaredField("b");
				animationid.setAccessible(true);
				animationid.set(animation, 0);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(animation);
			} catch (Exception ex) {
			}
			WoolBattleNPC.this.getSender().sendEmotePacket(p, Emote.KARATE);
		}
	}
}
