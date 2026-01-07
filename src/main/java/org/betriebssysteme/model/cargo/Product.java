package org.betriebssysteme.model.cargo;

public enum Product implements Cargo {
    DRIVE_HOUSING,
    CONTROL_HOUSING,
    DRIVE_PCB,
    CONTROL_PCB,
    DRIVE_UNIT,
    CONTROL_UNIT,
    SHIPPING_PACKAGE,
    SCRAP;

    public CargoTyp getCargoTyp() {
        return CargoTyp.PRODUCT;
    }
}
