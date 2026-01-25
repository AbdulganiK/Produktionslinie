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

    public static final String DEFAULT_CONFIG_RESOURCE = "assets/config/productionConfigs/productionConfig.json";
    public static final String RECIPES_CONFIG_RESOURCE = "assets/config/recipesConfigs/recipes.json";

    // Cache für bereits geladene Configs (key = resource path)
    private static final Map<String, JsonNode> CACHE = new ConcurrentHashMap<>();

    /**
     * Lädt oder lädt neu alle bekannten Ressourcen (DEFAULT + RECIPES).
     * Wirft IOException bei IO-/Parsing-Fehlern.
     */
    public static synchronized void reloadAll() throws IOException {
        reload(DEFAULT_CONFIG_RESOURCE);
        reload(RECIPES_CONFIG_RESOURCE);
    }

    /**
     * Lädt die angegebene Resource neu (favorisiert die Nutzerdaten im Benutzerordner).
     * Das Ergebnis wird im Cache abgelegt.
     *
     * @param resourceName Pfad innerhalb der Ressourcen, z.B. assets/...
     * @throws IOException bei Lesefehlern / Parsingfehlern
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
     * Liefert die JsonNode zur Resource. Nutzt Cache und lädt bei Bedarf neu.
     * Wirft RuntimeException bei Fehlern (bewahrt bisherigen API-Stil).
     *
     * @param resourceName Resource-Pfad
     * @return JsonNode der geladenen Konfiguration
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
     * Liefert den reinen Text der Config (Nutzerdatei bevorzugt, sonst Resource).
     *
     * @param resourceName Resource-Pfad
     * @return Inhalt als String
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
     * Pfad unterhalb des Benutzerordners, in dem Nutzerconfigs abgelegt werden.
     *
     * @param resourceName Resource-Pfad
     * @return Path zu z.\,B. {user.home}/.betriebssysteme/{Dateiname}
     */
    public static Path getUserConfigPath(String resourceName) {
        String fileName = Path.of(resourceName).getFileName().toString();
        return Path.of(System.getProperty("user.home"), ".betriebssysteme", fileName);
    }

    /**
     * Optional: Cache leeren.
     */
    public static void clearCache() {
        CACHE.clear();
    }
}