package org.betriebssysteme.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JSONConfig {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode loadConfig(String resourceName) {
        try (InputStream in = JSONConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new IllegalStateException(resourceName + " was not found!");
            return MAPPER.readTree(in);
        } catch (Exception e) {
            throw new RuntimeException("The file " + resourceName + "cannot be loaded!", e);
        }
    }
}
