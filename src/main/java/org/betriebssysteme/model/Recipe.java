package org.betriebssysteme.model;

import org.betriebssysteme.model.cargo.Cargo;

import java.util.HashMap;
import java.util.Map;

public record Recipe(
        int productionTime,
        Cargo productCargo,
        // Ingredients
        HashMap<Cargo, Integer> ingredients
) {
    public Map<Cargo, Integer> getInitalStorage(int initialQuantityPerIngredient) {
        Map<Cargo, Integer> initialStorage = new HashMap<>();
        for (Cargo ingredient : ingredients.keySet()) {
            initialStorage.put(ingredient, initialQuantityPerIngredient);
        }
        return initialStorage;
    }
}
