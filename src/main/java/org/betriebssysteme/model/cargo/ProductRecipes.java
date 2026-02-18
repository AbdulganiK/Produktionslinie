package org.betriebssysteme.model.cargo;

import com.fasterxml.jackson.databind.JsonNode;
import org.betriebssysteme.control.JSONConfig;
import org.betriebssysteme.model.Recipe;

import java.util.HashMap;

/**
 * The ProductRecipes class is responsible for loading and providing access to the recipes for creating products.
 * It reads the recipe configurations from a JSON file and initializes Recipe objects for each product.
 */
public class ProductRecipes {

    /**
     * The recipeConfig variable holds the JSON configuration for the recipes, loaded from a specified file path.
     * It is used to initialize the Recipe objects for each product.
     */
    JsonNode recipeConfig =
            JSONConfig.loadConfig("assets/config/recipesConfigs/RecipesConfigDefault.json")
                    .get("recipes");

    private final Recipe driveHousingRecipe = new Recipe(
            recipeConfig.get("driveHousingRecipe").get("productionTime").asInt(),
            Product.DRIVE_CASE,
            loadCargoMap(recipeConfig.get("driveHousingRecipe").get("components"))
    );


    private final Recipe controlHousingRecipe = new Recipe(
            recipeConfig.get("controlHousingRecipe").get("productionTime").asInt(),
            Product.CONTROL_CASE,
            loadCargoMap(recipeConfig.get("controlHousingRecipe").get("components"))
    );

    private final Recipe drivePcbRecipe = new Recipe(
            recipeConfig.get("drivePcbRecipe").get("productionTime").asInt(),
            Product.DRIVE_PCB,
            loadCargoMap(recipeConfig.get("drivePcbRecipe").get("components"))
    );

    private final Recipe controlPcbRecipe = new Recipe(
            recipeConfig.get("controlPcbRecipe").get("productionTime").asInt(),
            Product.CONTROL_PCB,
            loadCargoMap(recipeConfig.get("controlPcbRecipe").get("components"))
    );

    private final Recipe driveUnitRecipe = new Recipe(
            recipeConfig.get("driveUnitRecipe").get("productionTime").asInt(),
            Product.DRIVE_UNIT,
            loadCargoMap(recipeConfig.get("driveUnitRecipe").get("components"))
    );

    private final Recipe controlUnitRecipe = new Recipe(
            recipeConfig.get("controlUnitRecipe").get("productionTime").asInt(),
            Product.CONTROL_UNIT,
            loadCargoMap(recipeConfig.get("controlUnitRecipe").get("components"))
    );

    private final Recipe shippingPackageRecipe = new Recipe(
            recipeConfig.get("shippingPackageRecipe").get("productionTime").asInt(),
            Product.PACKAGE,
            loadCargoMap(recipeConfig.get("shippingPackageRecipe").get("components"))
    );

    /**
     * The loadCargoMap method takes a JsonNode representing the components of a recipe and converts it into a HashMap.
     *
     * @param node the JsonNode containing the components of a recipe
     * @return a HashMap mapping Cargo to their required amounts for the recipe
     */
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
