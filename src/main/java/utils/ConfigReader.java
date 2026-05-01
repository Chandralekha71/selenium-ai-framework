package utils;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
	
	private static Properties properties;
	
    // Static block to load file once when the class is first accessed
	static {
		try {
			properties = new Properties();
			FileInputStream file = new FileInputStream("src/main/resources/config.properties");
			properties.load(file);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {

		// Check the env variable — only use it if it is a real value (not blank or a placeholder)
		String env = System.getenv(key.toUpperCase());

		if (env != null && !env.isBlank()) {
			return env;
		}

		// Fallback to config file
		return properties.getProperty(key);
	}
	
    public static int     elementWaitTimeout(){ try { return Integer.parseInt(get("element_wait_timeout")); } catch (Exception e) { return 15; } }

}
