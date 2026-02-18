package org.betriebssysteme.model.cargo;

/**
 * The Product enum represents different types of products that can be used as cargo in the system.
 * Each product is associated with the CargoTyp.PRODUCT type.
 */
public enum Product implements Cargo {
    DRIVE_CASE,
    CONTROL_CASE,
    DRIVE_PCB,
    CONTROL_PCB,
    DRIVE_UNIT,
    CONTROL_UNIT,
    PACKAGE,
    SCRAP;

    public CargoTyp getCargoTyp() {
        return CargoTyp.PRODUCT;
    }
}
