package org.betriebssysteme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.betriebssysteme.control.ProductionController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatter);
        System.setProperty("log.filename", timestamp + ".log");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Application starting");
        ProductionController productionController = new ProductionController();
        productionController.createAllStations();
        productionController.createAllPersonnel();
        productionController.addAllToProductionHeadquarters();
        productionController.startProductionHeadquarters();
        logger.info("Application stopped");
    }
}
