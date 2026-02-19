// java
package org.betriebssysteme.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JSONConfig {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String DEFAULT_CONFIG_RESOURCE = "assets/config/productionConfigs/ProductionConfigDefault.json";
    public static final String RECIPES_CONFIG_RESOURCE = "assets/config/recipesConfigs/RecipesConfigDefault.json";

    // Cache for loaded configurations
    private static final Map<String, JsonNode> CACHE = new ConcurrentHashMap<>();

    /**
     * reload all default configurations (DefaultConfig und RecipesConfig).
     * @throws IOException if any of the files cannot be loaded or parsed
     */
    public static synchronized void reloadAll() throws IOException {
        reload(DEFAULT_CONFIG_RESOURCE);
        reload(RECIPES_CONFIG_RESOURCE);
    }

    /**
     * The Method reloads the configuration from the user file if it exists, otherwise from the classpath resource.
     * @param resourceName the name of the resource to reload
     * @throws IOException if the file cannot be loaded or parsed
     */
    public static synchronized void reload(String resourceName) throws IOException {
        Path userFile = getUserConfigPath(resourceName);
        JsonNode node;
        if (Files.exists(userFile)) {
            try (InputStream in = Files.newInputStream(userFile)) {
                node = MAPPER.readTree(in);
            }
        } else {
            try (InputStream in = JSONConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (in == null) {
                    throw new IOException(resourceName + " was not found on classpath");
                }
                node = MAPPER.readTree(in);
            }
        }
        CACHE.put(resourceName, node);
    }

    /**
     * Loads the configuration as JsonNode and returns it.
     * @param resourceName the name of the resource to load
     * @return the loaded configuration as JsonNode
     */
    public static JsonNode loadConfig(String resourceName) {
        JsonNode n = CACHE.get(resourceName);
        if (n != null) return n;
        try {
            reload(resourceName);
            return CACHE.get(resourceName);
        } catch (IOException e) {
            throw new RuntimeException("The file " + resourceName + " cannot be loaded!", e);
        }
    }

    /**
     * Loads the configuration as text and returns it.
     * @param resourceName the name of the resource to load
     * @return the loaded configuration as text
     */
    public static String loadConfigText(String resourceName) {
        try {
            Path userFile = getUserConfigPath(resourceName);
            if (Files.exists(userFile)) {
                return Files.readString(userFile, StandardCharsets.UTF_8);
            }
            try (InputStream in = JSONConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (in == null) throw new IOException(resourceName + " was not found!");
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("The file " + resourceName + " cannot be loaded as text!", e);
        }
    }

    /**
     * The method getUserConfigPath returns the path to the user configuration file for the given resource name.
     * The user configuration file is located in the user's home directory under ".betriebssysteme"
     * and has the same file name as the resource.
     * @param resourceName the name of the resource for which to get the user configuration path
     * @return the path to the user configuration file
     */
    public static Path getUserConfigPath(String resourceName) {
        String fileName = Path.of(resourceName).getFileName().toString();
        return Path.of(System.getProperty("user.home"), ".betriebssysteme", fileName);
    }
}