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
import java.util.Map;

public class ProductionController {
    private MainDepot mainDepot;
    private Supplier supplier;
    private ProductionHeadquarters productionHeadquarters;
    private ProductionMaschine productionMaschine;

    public void createAllStations() {
        productionHeadquarters = new ProductionHeadquarters();
        HashMap<Cargo , Integer> initialStorage = new HashMap<>();
        initialStorage.put(Material.GLUE, 1);
        initialStorage.put(Material.PLASTIC, 2);
        if (initialStorage == null){
            System.out.println("No initial storage found");
        }
        mainDepot = new MainDepot(10);
        Recipe recipe = new Recipe(
                5000,
                Product.SCRAP,
                initialStorage
        );
        if (productionHeadquarters == null){
            System.out.println("No production headquarters found");
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
        supplier = new Supplier(11, mainDepot, 100, 100, 100);
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
