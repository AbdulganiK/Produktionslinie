package org.betriebssysteme.model.cargo;

/**
 * The Cargo interface represents a general cargo type that can be either a Material or a Product
 * It provides a method to retrieve the CargoTyp, which indicates whether the cargo is a Material or a Product.
 */
public interface Cargo {
    /**
     * Returns the CargoTyp of this Cargo instance.
     * @return the CargoTyp of this Cargo
     */
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

    /**
     * Returns the CargoTyp of this Cargo instance.
     * @return the CargoTyp of this Cargo
     */
    CargoTyp getCargoTyp();
}
