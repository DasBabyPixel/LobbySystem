package eu.darkcube.system.lobbysystem.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class MojangAPI {

	public static String getCurrentName(UUID player) throws IOException {
		return getJsonURL(new URL("https://api.mojang.com/user/profiles/" + player.toString().replace("-", "") + "/names"))
				.getAsJsonArray()
				.get(0)
				.getAsJsonObject()
				.get("name")
				.getAsJsonPrimitive()
				.getAsString();
	}

	public static JsonElement getJsonURL(URL url) throws IOException {
		return new Gson().fromJson(getURLContent(url), JsonElement.class);
	}

	public static String getURLContent(URL url) throws IOException {
		StringBuilder b = new StringBuilder();
		URLConnection con = url.openConnection();
		BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
		String line;
		while ((line = r.readLine()) != null) {
			b.append(line).append(System.lineSeparator());
		}
		return b.toString();
	}

	public static String decodeBase64(String base64) {
		return new String(Base64.getDecoder().decode(base64));
	}
}
