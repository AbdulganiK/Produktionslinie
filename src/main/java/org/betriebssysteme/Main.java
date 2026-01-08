package org.betriebssysteme;

import org.betriebssysteme.control.ProductionController;

public class Main {
    public static void main(String[] args) {
        ProductionController productionController = new ProductionController();
        productionController.createAllStations();
        productionController.createAllPersonnel();
        productionController.addAllToProductionHeadquarters();
        productionController.startProductionHeadquarters();
    }
}