package org.betriebssysteme.control;
import com.fasterxml.jackson.databind.JsonNode;
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
    private WarehouseClerk warehouseClerk3;
    private WarehouseClerk warehouseClerk4;

    private final JsonNode productionConfigData;

    public ProductionController() {
        this.productionConfigData = JSONConfig.loadConfig("assets/config/productionConfig.json");
        logger.info("ProductionController initialized");
    }

    public void createAllStations() {
        JsonNode stations = productionConfigData.get("stations");

        JsonNode md = stations.get("mainDepot");
        mainDepot = new MainDepot(
                md.get("identificationNumber").asInt(),
                md.get("maxStorageCapacity").asInt(),
                md.get("initialStorageCapacity").asInt()
        );

        JsonNode duHouse = stations.get("driveUnitHouseProductionMaschine");
        driveUnitHouseProductionMaschine = new ProductionMaschine(
                duHouse.get("identificationNumber").asInt(),
                duHouse.get("timeToSleep").asInt(),
                duHouse.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getDriveHousingRecipe(),
                duHouse.get("initialQuantityOfProduct").asInt(),
                duHouse.get("maschinePriority").asInt()
        );

        JsonNode duPcb = stations.get("driveUnitCircuitBoardProductionMaschine");
        driveUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                duPcb.get("identificationNumber").asInt(),
                duPcb.get("timeToSleep").asInt(),
                duPcb.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getDrivePcbRecipe(),
                duPcb.get("initialQuantityOfProduct").asInt(),
                duPcb.get("maschinePriority").asInt()
        );

        JsonNode duUnit = stations.get("driveUnitProductionMaschine");
        driveUnitProductionMaschine = new ProductionMaschine(
                duUnit.get("identificationNumber").asInt(),
                duUnit.get("timeToSleep").asInt(),
                duUnit.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getDriveUnitRecipe(),
                duUnit.get("initialQuantityOfProduct").asInt(),
                duUnit.get("maschinePriority").asInt()
        );

        JsonNode cuHouse = stations.get("controlUnitHouseProductionMaschine");
        controlUnitHouseProductionMaschine = new ProductionMaschine(
                cuHouse.get("identificationNumber").asInt(),
                cuHouse.get("timeToSleep").asInt(),
                cuHouse.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getControlHousingRecipe(),
                cuHouse.get("initialQuantityOfProduct").asInt(),
                cuHouse.get("maschinePriority").asInt()
        );

        JsonNode cuPcb = stations.get("controlUnitCircuitBoardProductionMaschine");
        controlUnitCircuitBoardProductionMaschine = new ProductionMaschine(
                cuPcb.get("identificationNumber").asInt(),
                cuPcb.get("timeToSleep").asInt(),
                cuPcb.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getControlPcbRecipe(),
                cuPcb.get("initialQuantityOfProduct").asInt(),
                cuPcb.get("maschinePriority").asInt()
        );

        JsonNode cuUnit = stations.get("controlUnitProductionMaschine");
        controlUnitProductionMaschine = new ProductionMaschine(
                cuUnit.get("identificationNumber").asInt(),
                cuUnit.get("timeToSleep").asInt(),
                cuUnit.get("maxStorageCapacity").asInt(),
                null,
                productRecipes.getControlUnitRecipe(),
                cuUnit.get("initialQuantityOfProduct").asInt(),
                cuUnit.get("maschinePriority").asInt()
        );

        JsonNode cuQc = stations.get("controlUnitQualityControlMachine");
        controlUnitQualityControlMachine = new ControlMachine(
                cuQc.get("identificationNumber").asInt(),
                cuQc.get("timeToSleep").asInt(),
                cuQc.get("maxStorageCapacity").asInt(),
                cuQc.get("initialQuantityOfProduct").asInt(),
                Product.CONTROL_UNIT,
                null,
                cuQc.get("productionTime").asInt(),
                cuQc.get("probilityOfDefectPercent").asInt(),
                cuQc.get("maschinePriority").asInt()
        );

        JsonNode duQc = stations.get("driveUnitQualityControlMachine");
        driveUnitQualityControlMachine = new ControlMachine(
                duQc.get("identificationNumber").asInt(),
                duQc.get("timeToSleep").asInt(),
                duQc.get("maxStorageCapacity").asInt(),
                duQc.get("initialQuantityOfProduct").asInt(),
                Product.DRIVE_UNIT,
                null,
                duQc.get("productionTime").asInt(),
                duQc.get("probilityOfDefectPercent").asInt(),
                duQc.get("maschinePriority").asInt()
        );

        JsonNode pack = stations.get("packagingMaschine");
        packagingMaschine = new PackagingMaschine(
                pack.get("identificationNumber").asInt(),
                pack.get("timeToSleep").asInt(),
                pack.get("maxStorageCapacity").asInt(),
                null,
                pack.get("initialQuantityOfProduct").asInt(),
                pack.get("productionTime").asInt(),
                productRecipes.getShippingPackageRecipe(),
                pack.get("maschinePriority").asInt()
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
        JsonNode personnel = productionConfigData.get("personnel");

        JsonNode sup = personnel.get("supplier");
        JsonNode mainDepotNode = productionConfigData.get("stations").get("mainDepot");
        supplier = new Supplier(
                sup.get("identificationNumber").asInt(),
                sup.get("supplyInterval_ms").asInt(),
                sup.get("supplyTimer_ms").asInt(),
                sup.get("travelTimer_ms").asInt(),
                mainDepotNode.get("identificationNumber").asInt()
        );

        JsonNode w1 = personnel.get("warehouseClerk1");
        warehouseClerk1 = new WarehouseClerk(
                w1.get("identificationNumber").asInt(),
                w1.get("timeForTravel_ms").asInt(),
                w1.get("timeForTask_ms").asInt(),
                w1.get("timeForSleep_ms").asInt()
        );

        JsonNode w2 = personnel.get("warehouseClerk2");
        warehouseClerk2 = new WarehouseClerk(
                w2.get("identificationNumber").asInt(),
                w2.get("timeForTravel_ms").asInt(),
                w2.get("timeForTask_ms").asInt(),
                w2.get("timeForSleep_ms").asInt()
        );

        JsonNode w3 = personnel.get("warehouseClerk3");
        warehouseClerk3 = new WarehouseClerk(
                w3.get("identificationNumber").asInt(),
                w3.get("timeForTravel_ms").asInt(),
                w3.get("timeForTask_ms").asInt(),
                w3.get("timeForSleep_ms").asInt()
        );

        JsonNode w4 = personnel.get("warehouseClerk4");
        warehouseClerk4 = new WarehouseClerk(
                w4.get("identificationNumber").asInt(),
                w4.get("timeForTravel_ms").asInt(),
                w4.get("timeForTask_ms").asInt(),
                w4.get("timeForSleep_ms").asInt()
        );
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
        productionHeadquarters.addPersonnel(warehouseClerk3);
        productionHeadquarters.addPersonnel(warehouseClerk4);
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
