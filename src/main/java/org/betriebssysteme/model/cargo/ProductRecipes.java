package org.betriebssysteme.model.cargo;

import com.fasterxml.jackson.databind.JsonNode;
import org.betriebssysteme.control.JSONConfig;
import org.betriebssysteme.model.Recipe;

import java.util.HashMap;

public class ProductRecipes {

    JsonNode recipeConfig =
            JSONConfig.loadConfig("assets/config/recipes.json")
                    .get("recipes");

    private Recipe driveHousingRecipe = new Recipe(
            recipeConfig.get("driveHousingRecipe").get("productionTime").asInt(),
            Product.DRIVE_CASE,
            loadCargoMap(recipeConfig.get("driveHousingRecipe").get("components"))
    );


    private Recipe controlHousingRecipe = new Recipe(
            recipeConfig.get("controlHousingRecipe").get("productionTime").asInt(),
            Product.CONTROL_CASE,
            new HashMap<Cargo, Integer>() {{
                put(Material.GLUE, 1);
                put(Material.PLASTIC, 2);
            }}
    );

    private Recipe drivePcbRecipe = new Recipe(
            recipeConfig.get("drivePcbRecipe").get("productionTime").asInt(),
            Product.DRIVE_PCB,
            new HashMap<Cargo, Integer>() {{
                put(Material.PCBS, 2);
                put(Material.MOTORS, 2);
            }}
    );

    private Recipe controlPcbRecipe = new Recipe(
            recipeConfig.get("controlPcbRecipe").get("productionTime").asInt(),
            Product.CONTROL_PCB,
            new HashMap<Cargo, Integer>() {{
                put(Material.DISPLAYS, 1);
                put(Material.PCBS, 2);
            }}
    );

    private Recipe driveUnitRecipe = new Recipe(
            recipeConfig.get("driveUnitRecipe").get("productionTime").asInt(),
            Product.DRIVE_UNIT,
            new HashMap<Cargo, Integer>() {{
                put(Product.DRIVE_CASE, 1);
                put(Product.DRIVE_PCB, 1);
            }}
    );

    private Recipe controlUnitRecipe = new Recipe(
            recipeConfig.get("controlUnitRecipe").get("productionTime").asInt(),
            Product.CONTROL_UNIT,
            new HashMap<Cargo, Integer>() {{
                put(Product.CONTROL_CASE, 1);
                put(Product.CONTROL_PCB, 1);
            }}
    );

    private Recipe shippingPackageRecipe = new Recipe(
            recipeConfig.get("shippingPackageRecipe").get("productionTime").asInt(),
            Product.PACKAGE,
            new HashMap<Cargo, Integer>() {{
                put(Material.WRAPPING, 3);
                put(Product.DRIVE_UNIT, 1);
                put(Product.CONTROL_UNIT, 1);
            }}
    );

    private HashMap<Cargo, Integer> loadCargoMap(JsonNode node) {
        HashMap<Cargo, Integer> map = new HashMap<>();

        node.fields().forEachRemaining(entry -> {
            Cargo cargo = Cargo.valueOf(entry.getKey());
            int amount = entry.getValue().asInt();
            map.put(cargo, amount);
        });

        return map;
    }


    public Recipe getDriveHousingRecipe() {
        return driveHousingRecipe;
    }

    public Recipe getControlHousingRecipe() {
        return controlHousingRecipe;
    }

    public Recipe getDrivePcbRecipe() {
        return drivePcbRecipe;
    }

    public Recipe getControlPcbRecipe() {
        return controlPcbRecipe;
    }

    public Recipe getDriveUnitRecipe() {
        return driveUnitRecipe;
    }

    public Recipe getControlUnitRecipe() {
        return controlUnitRecipe;
    }

    public Recipe getShippingPackageRecipe() {
        return shippingPackageRecipe;
    }
}
