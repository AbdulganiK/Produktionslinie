package org.betriebssysteme.model.cargo;

import org.betriebssysteme.model.Recipe;

import java.util.HashMap;

public class ProductRecipes {
    private Recipe driveHousingRecipe = new Recipe(
            7000,
            Product.DRIVE_HOUSING,
            new HashMap<Cargo, Integer>() {{
                put(Material.GLUE, 1);
                put(Material.PLASTIC, 2);
            }}
    );

    private Recipe controlHousingRecipe = new Recipe(
            6000,
            Product.CONTROL_HOUSING,
            new HashMap<Cargo, Integer>() {{
                put(Material.GLUE, 1);
                put(Material.PLASTIC, 2);
            }}
    );

    private Recipe drivePcbRecipe = new Recipe(
            8000,
            Product.DRIVE_PCB,
            new HashMap<Cargo, Integer>() {{
                put(Material.CIRCUIT_BOARD_COMPONENTS, 2);
                put(Material.MOTORS, 2);
            }}
    );

    private Recipe controlPcbRecipe = new Recipe(
            7500,
            Product.CONTROL_PCB,
            new HashMap<Cargo, Integer>() {{
                put(Material.DISPLAYS, 1);
                put(Material.CIRCUIT_BOARD_COMPONENTS, 2);
            }}
    );

    private Recipe driveUnitRecipe = new Recipe(
            10000,
            Product.DRIVE_UNIT,
            new HashMap<Cargo, Integer>() {{
                put(Product.DRIVE_HOUSING, 1);
                put(Product.DRIVE_PCB, 1);
            }}
    );

    private Recipe controlUnitRecipe = new Recipe(
            9500,
            Product.CONTROL_UNIT,
            new HashMap<Cargo, Integer>() {{
                put(Product.CONTROL_HOUSING, 1);
                put(Product.CONTROL_PCB, 1);
            }}
    );

    private Recipe shippingPackageRecipe = new Recipe(
            2000,
            Product.SHIPPING_PACKAGE,
            new HashMap<Cargo, Integer>() {{
                put(Material.PACKING_MATERIAL, 3);
                put(Product.DRIVE_UNIT, 1);
                put(Product.CONTROL_UNIT, 1);
            }}
    );

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
}
