package org.betriebssysteme.control;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.cargo.ProductRecipes;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.ControlMachine;
import org.betriebssysteme.model.stations.MainDepot;
import org.betriebssysteme.model.stations.PackagingMaschine;
import org.betriebssysteme.model.stations.ProductionMaschine;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionController {
    private MainDepot mainDepot;
    private Supplier supplier;
    private ProductionHeadquarters productionHeadquarters;
    private static Logger logger;
    private ProductRecipes productRecipes = new ProductRecipes();

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

    public ProductionController() {
        this.logger = LoggerFactory.getLogger("ProductionController");
        logger.info("ProductionController initialized");
        this.productionHeadquarters = new ProductionHeadquarters();
    }

    public void createAllStations() {
        productionHeadquarters = new ProductionHeadquarters();
        mainDepot = new MainDepot(100, 50);
        driveUnitHouseProductionMaschine = new ProductionMaschine(
                21,
                500,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getDriveHousingRecipe(),
                5
                );
        driveUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                22,
                700,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getDrivePcbRecipe(),
                5
                );
        driveUnitProductionMaschine = new ProductionMaschine(
                23,
                1000,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getDriveUnitRecipe(),
                5
                );
        controlUnitHouseProductionMaschine = new ProductionMaschine(
                24,
                500,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getControlHousingRecipe(),
                5
                );
        controlUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                25,
                700,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getControlPcbRecipe(),
                5
                );
        controlUnitProductionMaschine = new ProductionMaschine(
                26,
                1000,
                25,
                 productionHeadquarters,
                null,
                productRecipes.getControlUnitRecipe(),
                5
                );
        controlUnitQualityControlMachine = new ControlMachine(
                31,
                500,
                10,
                5,
                Product.CONTROL_UNIT,
                null,
                800,
                 productionHeadquarters,
                30
                );
        driveUnitQualityControlMachine = new ControlMachine(
                32,
                500,
                10,
                5,
                Product.DRIVE_UNIT,
                null,
                800,
                 productionHeadquarters,
                25
        );
        packagingMaschine = new PackagingMaschine(
                40,
                1500,
                15,
                 productionHeadquarters,
                null,
                10,
                1200,
                productRecipes.getShippingPackageRecipe()
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
                10000,
                1000,
                1000);
        warehouseClerk1 = new WarehouseClerk(12,
                1000,
                2000,
                1000,
                productionHeadquarters);
    }

    public void addAllToProductionHeadquarters() {
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
    }

    public void startProductionHeadquarters() {
        productionHeadquarters.startAllStations();
        warehouseClerk1.setProductionHeadquarters(productionHeadquarters);
        productionHeadquarters.startAllPersonnel();
    }
}
