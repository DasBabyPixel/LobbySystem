package eu.darkcube.system.lobbysystem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.Charsets;

public enum Language {

	ENGLISH(Locale.ENGLISH), GERMAN(Locale.GERMAN)

	;

	private Locale locale;
	private ResourceBundle bundle;

	private Language(Locale locale) {
		this.locale = locale;
		try {
			if (Bundle.parent == null) {
				Bundle.parent = new PropertyResourceBundle(reader(stream("messages.properties")));
			}
//			bundle = ResourceBundle.getBundle("messages", this.locale, , Control.getNoFallbackControl(Control.FORMAT_DEFAULT));
			InputStream in = stream("messages_" + locale.getLanguage() + ".properties");
			if (in == null) {
				this.bundle = Bundle.parent;
			} else {
				final ResourceBundle bundle = new Bundle(reader(in));
				this.bundle = bundle;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static InputStream stream(String path) {
		return Language.class.getClassLoader().getResourceAsStream(path);
	}

	private static InputStreamReader reader(InputStream in) {
		return new InputStreamReader(in, Charsets.UTF_8);
	}

	private static class Bundle extends PropertyResourceBundle {

		private static ResourceBundle parent;

		public Bundle(Reader reader) throws IOException {
			super(reader);
			setParent(parent);
		}
	}

	public ResourceBundle getBundle() {
		return bundle;
	}

	public Locale getLocale() {
		return locale;
	}

	public static Language fromString(String language) {
		for (Language l : Language.values()) {
			if (l.name().equalsIgnoreCase(language)) {
				return l;
			}
		}
		return GERMAN;
	}
}
