package org.betriebssysteme.model.cargo;

public enum Material implements Cargo {
    PLASTIC,
    CIRCUIT_BOARD_COMPONENTS,
    DISPLAYS,
    MOTORS,
    GLUE,
    PACKING_MATERIAL;

    @Override
    public CargoTyp getCargoTyp() {
        return CargoTyp.MATERIAL;
    }
}
