package lk.thefurniturestore.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Env {
    private static final Properties APP_PROPERTIES = new Properties();
    private static final Properties DOTENV_PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = Env.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("app.properties was not found on the classpath");
            }
            APP_PROPERTIES.load(inputStream);
            loadDotEnv();
        } catch (IOException e) {
            throw new RuntimeException("Application properties loading failed: " + e.getMessage());
        }
    }

    public static String get(String key){
        String environmentValue = System.getenv(toEnvironmentKey(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String dotEnvValue = DOTENV_PROPERTIES.getProperty(toEnvironmentKey(key));
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return dotEnvValue;
        }

        return APP_PROPERTIES.getProperty(key);
    }

    public static String require(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + toEnvironmentKey(key));
        }
        return value;
    }

    private static String toEnvironmentKey(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }

    private static void loadDotEnv() throws IOException {
        Path dotEnvPath = Path.of(".env");
        if (!Files.isRegularFile(dotEnvPath)) {
            return;
        }

        for (String line : Files.readAllLines(dotEnvPath)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = trimmed.substring(0, separator).trim();
            String value = trimmed.substring(separator + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            DOTENV_PROPERTIES.setProperty(key, value);
        }
    }
    public static Properties getAppProperties(){
        return APP_PROPERTIES;
    }
}
