package org.betriebssysteme.model;

public enum MaterialType {
    PLASTIC(1),
    RUBBER(1),
    SMD_COMPONENTS(1),
    DISPLAYS(5),
    MOTORS(5),
    PCBS(10),
    SENSORS(2),
    LITHIUM_BATTERIES(8),
    PACKETS(15),
    SCRAP(5),
    GLUE(1),
    PACKING_MATERIAL(5);

    private int weight_and_size_heuristic;

    MaterialType(int weight_and_size_heuristic) {
        this.weight_and_size_heuristic = weight_and_size_heuristic;
    }

    public int getWeightAndSizeHeuristic() {
        return weight_and_size_heuristic;
    }
}
