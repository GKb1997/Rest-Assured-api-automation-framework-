package utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties = new Properties();

    static {
        try {
            InputStream input =
                    ConfigReader.class
                            .getClassLoader()
                            .getResourceAsStream("config/config.properties");

            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {

        // 1️⃣ JVM arguments (highest priority)
        String value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 2️⃣ Environment variables (Jenkins / GitHub)
        String envKey = key.replace('.', '_').toUpperCase();

        value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 3️⃣ Fallback to config.properties (NON-SECRETS ONLY)
        return properties.getProperty(key);
    }
}
