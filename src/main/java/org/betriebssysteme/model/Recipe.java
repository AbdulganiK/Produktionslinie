package org.betriebssysteme.model;

import org.betriebssysteme.model.cargo.Cargo;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a recipe for producing a product from ingredients.
 *
 * @param productionTime The time required to produce the product.
 * @param productCargo   The cargo representing the product.
 * @param ingredients    A map of ingredients required for the recipe, with their quantities.
 */
public record Recipe(
        int productionTime,
        Cargo productCargo,
        HashMap<Cargo, Integer> ingredients
) {

    /**
     * Generates the initial storage map for the ingredients with a specified initial quantity.
     *
     * @param initialQuantityPerIngredient The initial quantity for each ingredient.
     * @return A map containing the ingredients and their initial quantities.
     */
    public Map<Cargo, Integer> getInitalStorage(int initialQuantityPerIngredient) {
        Map<Cargo, Integer> initialStorage = new HashMap<>();
        for (Cargo ingredient : ingredients.keySet()) {
            initialStorage.put(ingredient, initialQuantityPerIngredient);
        }
        return initialStorage;
    }

    /**
     * Generates the initial storage map for the ingredients with a specified initial quantity and a storage entry for the product with zero quantity.
     *
     * @param initialQuantityPerIngredient The initial quantity for each ingredient.
     * @return A map containing the ingredients with their initial quantities and the product with zero quantity.
     */
    public Map<Cargo, Integer> getInitalStorageWithProduct(int initialQuantityPerIngredient) {
        Map<Cargo, Integer> initialStorage = new HashMap<>();
        for (Cargo ingredient : ingredients.keySet()) {
            initialStorage.put(ingredient, initialQuantityPerIngredient);
        }
        initialStorage.put(productCargo, 0);
        return initialStorage;
    }
}
