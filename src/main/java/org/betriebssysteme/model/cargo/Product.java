package org.betriebssysteme.model.cargo;

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
