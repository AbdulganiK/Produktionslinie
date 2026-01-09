package org.betriebssysteme.control;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.ProductRecipes;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.stations.MainDepot;
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

    public ProductionController() {
        this.logger = LoggerFactory.getLogger("ProductionController");
        logger.info("ProductionController initialized");
    }

    public void createAllStations() {
        productionHeadquarters = new ProductionHeadquarters();
        mainDepot = new MainDepot(10);
        driveUnitHouseProductionMaschine = new ProductionMaschine(
                21,
                500,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getDriveHousingRecipe()
                );
        driveUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                22,
                700,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getDrivePcbRecipe()
                );
        driveUnitProductionMaschine = new ProductionMaschine(
                23,
                1000,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getDriveUnitRecipe()
                );
        controlUnitHouseProductionMaschine = new ProductionMaschine(
                24,
                500,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getControlHousingRecipe()
                );
        controlUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                25,
                700,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getControlPcbRecipe()
                );
        controlUnitProductionMaschine = new ProductionMaschine(
                26,
                1000,
                10,
                 productionHeadquarters,
                null,
                productRecipes.getControlUnitRecipe()
                );
        setNextMachines();
    }

    public void setNextMachines() {
        driveUnitHouseProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        driveUnitCircuitBoardProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        controlUnitHouseProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
        controlUnitCircuitBoardProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
    }

    public void createAllPersonnel() {
        supplier = new Supplier(11,
                mainDepot,
                10000,
                1000,
                1000);
    }

    public void addAllToProductionHeadquarters() {
        productionHeadquarters = new ProductionHeadquarters();
        productionHeadquarters.addStation(mainDepot);
        productionHeadquarters.addStation(driveUnitHouseProductionMaschine);
        productionHeadquarters.addStation(driveUnitCircuitBoardProductionMaschine);
        productionHeadquarters.addStation(driveUnitProductionMaschine);
        productionHeadquarters.addStation(controlUnitHouseProductionMaschine);
        productionHeadquarters.addStation(controlUnitCircuitBoardProductionMaschine);
        productionHeadquarters.addStation(controlUnitProductionMaschine);
        productionHeadquarters.addPersonnel(supplier);
    }

    public void startProductionHeadquarters() {
        productionHeadquarters.startAllStations();
        productionHeadquarters.startAllPersonnel();
    }
}
