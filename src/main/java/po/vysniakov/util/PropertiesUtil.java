package po.vysniakov.util;

import po.vysniakov.exception.LoadPropertiesException;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    private PropertiesUtil() {
        throw new UnsupportedOperationException("Util class");
    }

    static {
        loadProperties();
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties() {
        try (var input = PropertiesUtil.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new LoadPropertiesException("Can't load properties", e);
        }
    }
}
