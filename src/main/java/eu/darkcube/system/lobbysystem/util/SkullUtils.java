package eu.darkcube.system.lobbysystem.util;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class SkullUtils {

	public static final void setSkullTextureId(ItemStack skull, String textureValue) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", textureValue));
		ItemMeta meta = skull.getItemMeta();
		try {
			Field f = meta.getClass().getDeclaredField("profile");
			f.setAccessible(true);
			f.set(meta, profile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		skull.setItemMeta(meta);
	}

}
