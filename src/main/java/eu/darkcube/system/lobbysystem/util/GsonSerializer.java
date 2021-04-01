package eu.darkcube.system.lobbysystem.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSerializer {

	public static final Gson gson;
	static {
		GsonBuilder b = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting();
		b.addSerializationExclusionStrategy(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				if (f.getAnnotation(DontSerialize.class) != null) {
					return true;
				}
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> var1) {
				return false;
			}
		});
		gson = b.create();
//		JsonDocument.GSON = gson;
	}

	public static @interface DontSerialize {

	}
}
