package org.betriebssysteme;
import org.slf4j.Logger;

import org.betriebssysteme.control.ProductionController;

public class Main {
    public static void main(String[] args) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
        logger.info("Starting Production Line Simulation");
        ProductionController productionController = new ProductionController();
        productionController.createAllStations();
        productionController.createAllPersonnel();
        productionController.addAllToProductionHeadquarters();
        productionController.startProductionHeadquarters();
    }
}