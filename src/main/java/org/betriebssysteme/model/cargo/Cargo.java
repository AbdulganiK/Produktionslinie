package org.betriebssysteme.model.cargo;

public interface Cargo {
    static Cargo valueOf(String key) {
        for (Material material : Material.values()) {
            if (material.name().equalsIgnoreCase(key)) {
                return material;
            }
        }
        for (Product product : Product.values()) {
            if (product.name().equalsIgnoreCase(key)) {
                return product;
            }
        }
        throw new IllegalArgumentException("No enum constant for key: " + key);
    }

    CargoTyp getCargoTyp();
}
