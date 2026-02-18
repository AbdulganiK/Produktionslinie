package org.betriebssysteme.control;
import com.fasterxml.jackson.databind.JsonNode;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.cargo.ProductRecipes;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.ControlMachine;
import org.betriebssysteme.model.stations.MainDepot;
import org.betriebssysteme.model.stations.PackagingMaschine;
import org.betriebssysteme.model.stations.ProductionMaschine;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ProductionController class is responsible for initializing and managing the production line based on a JSON configuration.
 * It creates all stations and personnel, sets up the production flow, and starts the production process.
 */
public class ProductionController {
    private static final Logger logger = LoggerFactory.getLogger("ProductionController");

    // JSON Config Keys
    private static final String STATIONS = "stations";
    private static final String PERSONNEL = "personnel";
    private static final String ID_NUMBER = "identificationNumber";
    private static final String TIME_TO_SLEEP = "timeToSleep";
    private static final String MAX_STORAGE = "maxStorageCapacity";
    private static final String INITIAL_STORAGE = "initialStorageCapacity";
    private static final String INITIAL_QUANTITY = "initialQuantityOfProduct";
    private static final String PRODUCTION_TIME = "productionTime";
    private static final String MACHINE_PRIORITY = "maschinePriority";
    private static final String DEFECT_PROBABILITY = "probabilityOfDefectPercent";

    // Objects with loaded Product Recipes
    private final ProductRecipes productRecipes = new ProductRecipes();
    // List with all suppliers, needed to add them to headquarters
    private final List<Supplier> suppliers = new ArrayList<>();
    // List with all warehouse clerks, needed to add them to headquarters
    private final List<WarehouseClerk> warehouseClerks = new ArrayList<>();
    // JSON data for production configuration
    private final JsonNode productionConfigData;

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatter);
        System.setProperty("log.filename", timestamp + ".log");
    }

    // Production Stations
    private MainDepot mainDepot;
    private ProductionMaschine driveUnitHouseProductionMaschine;
    private ProductionMaschine driveUnitCircuitBoardProductionMaschine;
    private ProductionMaschine driveUnitProductionMaschine;
    private ProductionMaschine controlUnitHouseProductionMaschine;
    private ProductionMaschine controlUnitCircuitBoardProductionMaschine;
    private ProductionMaschine controlUnitProductionMaschine;
    private ControlMachine controlUnitQualityControlMachine;
    private ControlMachine driveUnitQualityControlMachine;
    private PackagingMaschine packagingMaschine;


    // Constructor loads the production configuration and initializes the logger
    public ProductionController() {
        try {
            this.productionConfigData = JSONConfig.loadConfig("assets/config/productionConfigs/ProductionConfigDefault.json");
            logger.info("ProductionController initialized");
        } catch (RuntimeException e) {
            logger.error("Failed to load production configuration", e);
            throw new IllegalStateException("Production configuration could not be loaded. Application cannot start.", e);
        }
    }

    /**
     * Creates all stations based on the loaded JSON configuration.
     * @throws IllegalStateException if critical configuration sections are missing or if station creation fails.
     */
    public void createAllStations() {
        try {
            JsonNode stations = productionConfigData.get(STATIONS);
            if (stations == null) {
                throw new IllegalStateException("Stations configuration section is missing");
            }

            createMainDepot(stations);
            createProductionMachines(stations);
            createQualityControlMachines(stations);
            createPackagingMachine(stations);
            setNextMachines();
            logger.info("All stations created and next machines set");
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("All stations created and next machines set");
            }
        } catch (Exception e) {
            logger.error("Failed to create stations", e);
            throw new IllegalStateException("Station creation failed. Check configuration file.", e);
        }
    }

    /**
     * Creates the MainDepot station based on the JSON configuration. Validates the presence of required fields.
     * @param stations JsonNode containing the station's configuration
     * @throws IllegalArgumentException if the MainDepot configuration is missing or if required fields are not present
     */
    private void createMainDepot(JsonNode stations) {
        JsonNode md = stations.get("mainDepot");
        if (md == null) {
            throw new IllegalArgumentException("MainDepot configuration is missing");
        }
        mainDepot = new MainDepot(
                md.get(ID_NUMBER).asInt(),
                md.get(MAX_STORAGE).asInt(),
                md.get(INITIAL_STORAGE).asInt()
        );
        logger.info("MainDepot created with ID: {}", mainDepot.getIdentificationNumber());
    }

    /**
     * Creates all production machines based on the JSON configuration and the loaded product recipes.
     * @param stations JsonNode containing the station's configuration
     */
    private void createProductionMachines(JsonNode stations) {
        driveUnitHouseProductionMaschine = createProductionMaschine(
                stations.get("driveUnitHouseProductionMaschine"),
                productRecipes.getDriveHousingRecipe(),
                "DriveUnitHouseProductionMaschine"
        );

        driveUnitCircuitBoardProductionMaschine = createProductionMaschine(
                stations.get("driveUnitCircuitBoardProductionMaschine"),
                productRecipes.getDrivePcbRecipe(),
                "DriveUnitCircuitBoardProductionMaschine"
        );

        driveUnitProductionMaschine = createProductionMaschine(
                stations.get("driveUnitProductionMaschine"),
                productRecipes.getDriveUnitRecipe(),
                "DriveUnitProductionMaschine"
        );

        controlUnitHouseProductionMaschine = createProductionMaschine(
                stations.get("controlUnitHouseProductionMaschine"),
                productRecipes.getControlHousingRecipe(),
                "ControlUnitHouseProductionMaschine"
        );

        controlUnitCircuitBoardProductionMaschine = createProductionMaschine(
                stations.get("controlUnitCircuitBoardProductionMaschine"),
                productRecipes.getControlPcbRecipe(),
                "ControlUnitCircuitBoardProductionMaschine"
        );

        controlUnitProductionMaschine = createProductionMaschine(
                stations.get("controlUnitProductionMaschine"),
                productRecipes.getControlUnitRecipe(),
                "ControlUnitProductionMaschine"
        );
    }

    /**
     * Creates the quality control machines for both control units and drive units based on the JSON configuration.
     * @param stations JsonNode containing the station's configuration
     */
    private void createQualityControlMachines(JsonNode stations) {
        controlUnitQualityControlMachine = createControlMachine(
                stations.get("controlUnitQualityControlMachine"),
                Product.CONTROL_UNIT,
                "ControlUnitQualityControlMachine"
        );

        driveUnitQualityControlMachine = createControlMachine(
                stations.get("driveUnitQualityControlMachine"),
                Product.DRIVE_UNIT,
                "DriveUnitQualityControlMachine"
        );
    }

    /**
     * Creates the packaging machine based on the JSON configuration and the shipping package recipe.
     * @param stations JsonNode containing the station's configuration
     * @throws IllegalArgumentException if the packaging machine configuration is missing or if required fields are not present
     */
    private void createPackagingMachine(JsonNode stations) {
        JsonNode pack = stations.get("packagingMaschine");
        if (pack == null) {
            throw new IllegalArgumentException("PackagingMaschine configuration is missing");
        }
        packagingMaschine = new PackagingMaschine(
                pack.get(ID_NUMBER).asInt(),
                pack.get(TIME_TO_SLEEP).asInt(),
                pack.get(MAX_STORAGE).asInt(),
                null,
                pack.get(INITIAL_QUANTITY).asInt(),
                pack.get(PRODUCTION_TIME).asInt(),
                productRecipes.getShippingPackageRecipe(),
                pack.get(MACHINE_PRIORITY).asInt()
        );
        logger.info("PackagingMaschine created with ID: {}", packagingMaschine.getIdentificationNumber());
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("PackagingMaschine created with ID: " + packagingMaschine.getIdentificationNumber());
        }
    }

    /**
     * Helper method to create a production machine based on the provided JSON configuration and recipe.
     * @param config JsonNode containing the machine configuration
     * @param recipe Recipe object required for the production machine
     * @param machineName Name of the machine for logging purposes
     * @return A new instance of ProductionMaschine configured according to the provided JSON data
     * @throws IllegalArgumentException if the configuration is missing or if required fields are not present
     */
    private ProductionMaschine createProductionMaschine(JsonNode config, Recipe recipe, String machineName) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration for " + machineName + " is missing");
        }
        ProductionMaschine machine = new ProductionMaschine(
                config.get(ID_NUMBER).asInt(),
                config.get(TIME_TO_SLEEP).asInt(),
                config.get(MAX_STORAGE).asInt(),
                null,
                recipe,
                config.get(INITIAL_QUANTITY).asInt(),
                config.get(MACHINE_PRIORITY).asInt()
        );
        logger.info("{} created with ID: {}", machineName, machine.getIdentificationNumber());
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println(machineName + " created with ID: " + machine.getIdentificationNumber());
        }
        return machine;
    }

    /**
     * Helper method to create a control machine based on the provided JSON configuration and product type.
     * @param config JsonNode containing the machine configuration
     * @param product Product type that this control machine will be responsible for
     * @param machineName Name of the machine for logging purposes
     * @return A new instance of ControlMachine configured according to the provided JSON data
     * @throws IllegalArgumentException if the configuration is missing or if required fields are not present
     */
    private ControlMachine createControlMachine(JsonNode config, Product product, String machineName) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration for " + machineName + " is missing");
        }
        ControlMachine machine = new ControlMachine(
                config.get(ID_NUMBER).asInt(),
                config.get(TIME_TO_SLEEP).asInt(),
                config.get(MAX_STORAGE).asInt(),
                config.get(INITIAL_QUANTITY).asInt(),
                product,
                null,
                config.get(PRODUCTION_TIME).asInt(),
                config.get(DEFECT_PROBABILITY).asInt(),
                config.get(MACHINE_PRIORITY).asInt()
        );
        logger.info("{} created with ID: {}", machineName, machine.getIdentificationNumber());
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println(machineName + " created with ID: " + machine.getIdentificationNumber());
        }
        return machine;
    }

    /**
     * Sets the next machines for all production and control machines according to the production flow.
     */
    public void setNextMachines() {
        driveUnitHouseProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        driveUnitCircuitBoardProductionMaschine.setNextMaschine(driveUnitProductionMaschine);
        controlUnitHouseProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
        controlUnitCircuitBoardProductionMaschine.setNextMaschine(controlUnitProductionMaschine);
        controlUnitProductionMaschine.setNextMaschine(controlUnitQualityControlMachine);
        driveUnitProductionMaschine.setNextMaschine(driveUnitQualityControlMachine);
        controlUnitQualityControlMachine.setNextMaschine(packagingMaschine);
        driveUnitQualityControlMachine.setNextMaschine(packagingMaschine);
        logger.info("Next machines set for all production machines");
    }

    /**
     * Creates all personnel (suppliers and warehouse clerks) based on the loaded JSON configuration.
     * @throws IllegalStateException if critical configuration sections are missing or if personnel creation fails
     */
    public void createAllPersonnel() {
        try {
            JsonNode personnelNode = productionConfigData.get(PERSONNEL);
            if (personnelNode == null) {
                throw new IllegalStateException("Personnel configuration section is missing");
            }

            JsonNode mainDepotNode = productionConfigData.get(STATIONS).get("mainDepot");
            if (mainDepotNode == null || mainDepotNode.get(ID_NUMBER) == null) {
                throw new IllegalStateException("MainDepot ID is missing in configuration");
            }
            int mainDepotId = mainDepotNode.get(ID_NUMBER).asInt();

            createSuppliers(personnelNode, mainDepotId);
            createWarehouseClerks(personnelNode, mainDepotId);
            logger.info("All personnel created");
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("All personnel created");
            }
        } catch (Exception e) {
            logger.error("Failed to create personnel", e);
            throw new IllegalStateException("Personnel creation failed. Check configuration file.", e);
        }
    }

    /**
     * Helper method to create supplier personnel based on the provided JSON configuration.
     * @param personnelNode JsonNode containing the personnel configuration
     * @param mainDepotId The identification number of the main depot
     * @throws IllegalArgumentException if required, fields for suppliers are not present in the configuration
     */
    private void createSuppliers(JsonNode personnelNode, int mainDepotId) {
        suppliers.clear();
        JsonNode suppliersNode = personnelNode.get("suppliers");
        if (suppliersNode != null && suppliersNode.isArray()) {
            for (JsonNode supNode : suppliersNode) {
                suppliers.add(new Supplier(
                        supNode.get(ID_NUMBER).asInt(),
                        supNode.get("supplyInterval_ms").asInt(),
                        supNode.get("supplyTimer_ms").asInt(),
                        mainDepotId,
                        supNode.get("maxCapacity").asInt()
                ));
                logger.info("Supplier created with ID: {}", supNode.get(ID_NUMBER).asInt());
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("Supplier created with ID: " + supNode.get(ID_NUMBER).asInt());
                }
            }
        }
    }

    /**
     * Helper method to create warehouse clerk personnel based on the provided JSON configuration.
     * @param personnelNode JsonNode containing the personnel configuration
     * @param mainDepotId The identification number of the main depot to which warehouse clerks will be associated
     * @throws IllegalArgumentException if required, fields for warehouse clerks are not present in the configuration
     */
    private void createWarehouseClerks(JsonNode personnelNode, int mainDepotId) {
        warehouseClerks.clear();
        JsonNode clerksNode = personnelNode.get("warehouseClerks");
        if (clerksNode != null && clerksNode.isArray()) {
            for (JsonNode clerkNode : clerksNode) {
                warehouseClerks.add(new WarehouseClerk(
                        clerkNode.get(ID_NUMBER).asInt(),
                        clerkNode.get("timeForTask_ms").asInt(),
                        clerkNode.get("timeForSleep_ms").asInt(),
                        clerkNode.get("maxCapacity").asInt(),
                        mainDepotId
                ));
                logger.info("WarehouseClerk created with ID: {}", clerkNode.get(ID_NUMBER).asInt());
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("WarehouseClerk created with ID: " + clerkNode.get(ID_NUMBER).asInt());
                }
            }
        }
    }

    /**
     * Adds all created stations and personnel to the ProductionHeadquarters singleton instance.
     * This allows the headquarters to manage and coordinate all components of the production line.
     */
    public void addAllToProductionHeadquarters() {
        ProductionHeadquarters hq = ProductionHeadquarters.getInstance();
        addAllStationsToHeadquarters(hq);
        addAllPersonnelToHeadquarters(hq);
        logger.info("All stations and personnel added to ProductionHeadquarters");
    }

    /**
     * Helper method to add all created stations to the ProductionHeadquarters instance.
     * This includes the main depot, production machines, control machines, and packaging machine.
     * @param hq The ProductionHeadquarters instance to which the stations will be added
     */
    private void addAllStationsToHeadquarters(ProductionHeadquarters hq) {
        hq.addStation(mainDepot);
        hq.addStation(driveUnitHouseProductionMaschine);
        hq.addStation(driveUnitCircuitBoardProductionMaschine);
        hq.addStation(driveUnitProductionMaschine);
        hq.addStation(controlUnitHouseProductionMaschine);
        hq.addStation(controlUnitCircuitBoardProductionMaschine);
        hq.addStation(controlUnitProductionMaschine);
        hq.addStation(controlUnitQualityControlMachine);
        hq.addStation(driveUnitQualityControlMachine);
        hq.addStation(packagingMaschine);
    }

    /**
     * Helper method to add all created personnel (suppliers and warehouse clerks) to the ProductionHeadquarters instance.
     * This allows the headquarters to manage and coordinate the activities of all personnel in the production line.
     * @param hq The ProductionHeadquarters instance to which the personnel will be added
     */
    private void addAllPersonnelToHeadquarters(ProductionHeadquarters hq) {
        suppliers.forEach(hq::addPersonnel);
        warehouseClerks.forEach(hq::addPersonnel);
    }

    /**
     * Starts all stations and personnel in the ProductionHeadquarters.
     * This method sends the start signal to all components,
     * allowing the production line to begin operation.
     */
    public void startProductionHeadquarters() {
        ProductionHeadquarters hq = ProductionHeadquarters.getInstance();
        hq.startAllStations();
        hq.startAllPersonnel();
    }

    /**
     * Static method to create and start the entire production line.
     * This method serves as the main entry point for initializing the production system.
     * It creates an instance of ProductionController, sets up all stations and personnel, adds them to the headquarters, and starts the production process.
     * @throws RuntimeException if any critical error occurs during the initialization of the production line
     */
    public static void createProductionLine() {
        logger.info("Application starting");
        try {
            ProductionController controller = new ProductionController();
            controller.createAllStations();
            controller.createAllPersonnel();
            controller.addAllToProductionHeadquarters();
            controller.startProductionHeadquarters();
            logger.info("Production line created and started");
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("Production line created and started");
            }
        } catch (Exception e) {
            logger.error("Critical error during production line creation", e);
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.err.println("Failed to create production line: " + e.getMessage());
            }
            throw new RuntimeException("Production line initialization failed", e);
        }
    }
}