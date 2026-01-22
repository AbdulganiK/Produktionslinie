package org.betriebssysteme.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSONConfig {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final String DEFAULT_CONFIG_RESOURCE = "assets/config/productionConfig.json";
    public static final String RECIPES_CONFIG_RESOURCE = "assets/config/recipes.json";


    public static JsonNode loadConfig(String resourceName) {
        try{
            Path userFile = getUserConfigPath(resourceName);
            if (Files.exists(userFile)){
                try (InputStream in = Files.newInputStream(userFile)) {
                    return MAPPER.readTree(in);
                }
            }

            try (InputStream in = JSONConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (in == null) throw new IllegalStateException(resourceName + "was not found");
                return MAPPER.readTree(in);
            }
        }catch (Exception e) {
            throw new RuntimeException("The file " + resourceName + "cannot be loaded!", e);
        }
    }

    public static String loadConfigText(String resourceName) {
        try {
            Path userFile = getUserConfigPath(resourceName);
            if (Files.exists(userFile)) {
                return Files.readString(userFile, StandardCharsets.UTF_8);
            }

            try (InputStream in = JSONConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (in == null) throw new IllegalStateException(resourceName + " was not found!");
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("The file " + resourceName + " cannot be loaded as text!", e);
        }
    }

    public static Path getUserConfigPath(String resourceName) {
        return Path.of(System.getProperty("user.home"), ".betriebssysteme", resourceName);
    }
}
