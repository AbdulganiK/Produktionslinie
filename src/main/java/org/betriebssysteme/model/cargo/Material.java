package org.betriebssysteme.model.cargo;

/**
 * The Material enum represents different types of materials that can be used as cargo in the system.
 * Each material is associated with the CargoTyp.MATERIAL type.
 */
public enum Material implements Cargo {
    PLASTIC,
    PCBS,
    DISPLAYS,
    MOTORS,
    GLUE,
    WRAPPING;

    @Override
    public CargoTyp getCargoTyp() {
        return CargoTyp.MATERIAL;
    }
}
