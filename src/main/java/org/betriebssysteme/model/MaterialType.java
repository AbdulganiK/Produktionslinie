package org.betriebssysteme.model;

public enum MaterialType {
    PLASTIC(1),
    CIRCUIT_BOARD_COMPONETS(1),
    DISPLAYS(5),
    MOTORS(5),
    GLUE(1),
    PACKING_MATERIAL(5),
    REQUETED_EMPTYING(0);

    private int weight_and_size_heuristic;

    MaterialType(int weight_and_size_heuristic) {
        this.weight_and_size_heuristic = weight_and_size_heuristic;
    }

    public int getWeightAndSizeHeuristic() {
        return weight_and_size_heuristic;
    }
}
