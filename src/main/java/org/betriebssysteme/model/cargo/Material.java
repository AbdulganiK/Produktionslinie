package org.betriebssysteme.model.cargo;

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
