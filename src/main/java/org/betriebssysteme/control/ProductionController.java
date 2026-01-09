package org.betriebssysteme.control;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;
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
    private ProductionMaschine productionMaschine;
    private static Logger logger;

    public ProductionController() {
        this.logger = LoggerFactory.getLogger("ProductionController");
        logger.info("ProductionController initialized");
    }

    public void createAllStations() {
        productionHeadquarters = new ProductionHeadquarters();
        HashMap<Cargo , Integer> initialStorage = new HashMap<>();
        initialStorage.put(Material.GLUE, 1);
        initialStorage.put(Material.PLASTIC, 2);
        if (initialStorage == null){
            logger.error("initialStorage is null");
        }
        mainDepot = new MainDepot(10);
        Recipe recipe = new Recipe(
                5000,
                Product.SCRAP,
                initialStorage
        );
        if (productionHeadquarters == null){
        }
        productionMaschine = new ProductionMaschine(
                2,
                1000,
                10,
                productionHeadquarters,
                null,
                recipe
        );
        //
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
        productionHeadquarters.addStation(productionMaschine);
        productionHeadquarters.addPersonnel(supplier);
    }

    public void startProductionHeadquarters() {
        productionHeadquarters.startAllStations();
        productionHeadquarters.startAllPersonnel();
    }
}
