package org.betriebssysteme.control;

import org.betriebssysteme.Main;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.cargo.ProductRecipes;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.ControlMachine;
import org.betriebssysteme.model.stations.MainDepot;
import org.betriebssysteme.model.stations.PackagingMaschine;
import org.betriebssysteme.model.stations.ProductionMaschine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionController {
    private MainDepot mainDepot;
    private Supplier supplier;
    private ProductRecipes productRecipes = new ProductRecipes();

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatter);
        System.setProperty("log.filename", timestamp + ".log");
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductionController.class);

    // Production Maschinen
    private ProductionMaschine driveUnitHouseProductionMaschine;
    private ProductionMaschine driveUnitCircuitBoardProductionMaschine;
    private ProductionMaschine driveUnitProductionMaschine;
    private ProductionMaschine controlUnitHouseProductionMaschine;
    private ProductionMaschine controlUnitCircuitBoardProductionMaschine;
    private ProductionMaschine controlUnitProductionMaschine;
    private ControlMachine controlUnitQualityControlMachine;
    private ControlMachine driveUnitQualityControlMachine;
    private PackagingMaschine packagingMaschine;
    private WarehouseClerk warehouseClerk1;
    private WarehouseClerk warehouseClerk2;

    public ProductionController() {
        logger.info("ProductionController initialized");
    }

    public void createAllStations() {
        mainDepot = new MainDepot(100, 50);
        // erste Reihe Produktion
        driveUnitHouseProductionMaschine = new ProductionMaschine(
                21,
                500,
                25,
                null,
                productRecipes.getDriveHousingRecipe(),
                5,
                1
                );
        driveUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                22,
                700,
                25,
                null,
                productRecipes.getDrivePcbRecipe(),
                5,
                1
                );
        // Zweite Reihe
        driveUnitProductionMaschine = new ProductionMaschine(
                23,
                1000,
                25,
                null,
                productRecipes.getDriveUnitRecipe(),
                5,
                2
                );
        // erste Reihe
        controlUnitHouseProductionMaschine = new ProductionMaschine(
                24,
                500,
                25,
                null,
                productRecipes.getControlHousingRecipe(),
                5,
                1
                );
        controlUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                25,
                700,
                25,
                null,
                productRecipes.getControlPcbRecipe(),
                5,
                1
                );
        // Zweite Reihe
        controlUnitProductionMaschine = new ProductionMaschine(
                26,
                1000,
                25,
                null,
                productRecipes.getControlUnitRecipe(),
                5,
                2
                );
        // dritte Reihe
        controlUnitQualityControlMachine = new ControlMachine(
                31,
                500,
                10,
                5,
                Product.CONTROL_UNIT,
                null,
                800,
                30,
                3
                );
        driveUnitQualityControlMachine = new ControlMachine(
                32,
                500,
                10,
                5,
                Product.DRIVE_UNIT,
                null,
                800,
                30,
                3
        );
        // vierte
        packagingMaschine = new PackagingMaschine(
                41,
                1500,
                15,
                null,
                10,
                1200,
                productRecipes.getShippingPackageRecipe(),
                4
        );
        setNextMachines();
    }

    public void setNextMachines() {
        driveUnitHouseProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        driveUnitCircuitBoardProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        controlUnitHouseProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
        controlUnitCircuitBoardProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
        controlUnitProductionMaschine.setNextMaschine(controlUnitQualityControlMachine);
        driveUnitProductionMaschine.setNextMaschine(driveUnitQualityControlMachine);
        controlUnitQualityControlMachine.setNextMaschine(packagingMaschine);
        driveUnitQualityControlMachine.setNextMaschine(packagingMaschine);
    }

    public void createAllPersonnel() {
        supplier = new Supplier(11,
                mainDepot,
                1000,
                5000,
                2000);
        warehouseClerk1 = new WarehouseClerk(12,
                10000,
                5000,
                2000);
        warehouseClerk2 = new WarehouseClerk(13,
                10000,
                5000,
                2000);
    }

    public void addAllToProductionHeadquarters() {
        ProductionHeadquarters productionHeadquarters = ProductionHeadquarters.getInstance();
        productionHeadquarters.addStation(mainDepot);
        productionHeadquarters.addStation(driveUnitHouseProductionMaschine);
        productionHeadquarters.addStation(driveUnitCircuitBoardProductionMaschine);
        productionHeadquarters.addStation(driveUnitProductionMaschine);
        productionHeadquarters.addStation(controlUnitHouseProductionMaschine);
        productionHeadquarters.addStation(controlUnitCircuitBoardProductionMaschine);
        productionHeadquarters.addStation(controlUnitProductionMaschine);
        productionHeadquarters.addStation(controlUnitQualityControlMachine);
        productionHeadquarters.addStation(driveUnitQualityControlMachine);
        productionHeadquarters.addStation(packagingMaschine);
        productionHeadquarters.addPersonnel(supplier);
        productionHeadquarters.addPersonnel(warehouseClerk1);
        productionHeadquarters.addPersonnel(warehouseClerk2);
    }

    public void startProductionHeadquarters() {
        ProductionHeadquarters productionHeadquarters = ProductionHeadquarters.getInstance();
        productionHeadquarters.startAllStations();
        productionHeadquarters.startAllPersonnel();
    }

    public static void createProductionLine(){
        logger.info("Application starting");
        ProductionController productionController = new ProductionController();
        productionController.createAllStations();
        productionController.createAllPersonnel();
        productionController.addAllToProductionHeadquarters();
        productionController.startProductionHeadquarters();
        logger.info("Application stopped");
    }
}
