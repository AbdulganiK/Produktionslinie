package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.stations.MainDepot;
import org.slf4j.Logger;

import java.util.Map;

public class Supplier extends Thread implements Personnel {
    private int identificationNumber;
    private Status status;
    private Task task;
    private MainDepot mainDepot;
    private int originStationId;
    private int destinationStationId;
    private int supplyInterval_ms;
    private int supplyTimer_ms;
    private int travelTimer_ms;
    private Logger logger;

    public Supplier(int identificationNumber, MainDepot mainDepot, int supplyInterval_ms, int supplyTimer_ms, int travelTimer_ms) {
        this.identificationNumber = identificationNumber;
        this.mainDepot = mainDepot;
        this.supplyInterval_ms = supplyInterval_ms;
        this.supplyTimer_ms = supplyTimer_ms;
        this.travelTimer_ms = travelTimer_ms;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.status = Status.STOPPED;
        this.task = Task.JOBLESS;
        this.logger = org.slf4j.LoggerFactory.getLogger("Supplier-" + identificationNumber);
        logger.info("Supplier " + identificationNumber + " created");
    }

    private void supplyRoutine() {
        task = Task.DELIVERING;
        destinationStationId = mainDepot.getIdentificationNumber();
        logger.info("Supplier starting supply routine to Main Depot");
        try {
            Thread.sleep(travelTimer_ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        refillDepotAndCollectCargo();
        task = Task.TRANSPORTING;
        originStationId = mainDepot.getIdentificationNumber();
        destinationStationId = -1;
        try {
            Thread.sleep(travelTimer_ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        originStationId = -1;
        logger.info("Supplier finishing supply routine to Main Depot");
    }

    private void refillDepotAndCollectCargo() {
        try {
            Thread.sleep(supplyTimer_ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // TODO Implement the depot refilling logic
        logger.info("Supplier refilled depot and collected cargo");
    }

    // ============================================================================
    //Personnel methods
    @Override
    public int refillCargo(Cargo cargo, int quantity) {
        for (Material material : Material.values()) {
            mainDepot.resiveCargo(material, mainDepot.getMaxStorageCapacity());
        }
        logger.info("Depot refilled with materials");
        return 0;
    }

    @Override
    public int collectCargo(Cargo cargo, int quantity) {
        mainDepot.handOverCargo(Product.SCRAP, mainDepot.getMaxStorageCapacity());
        mainDepot.handOverCargo(Product.SHIPPING_PACKAGE, mainDepot.getMaxStorageCapacity());
        logger.info("Collected cargo from depot");
        return 0;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Map getInformationMap() {
        // TODO Implement the information map logic
        return Map.of();
    }

    @Override
    public int getDestinationStationId() {
        return destinationStationId;
    }

    @Override
    public int getOriginStationId() {
        return originStationId;
    }

    @Override
    public Task getCurrentTask() {
        return task;
    }

    @Override
    public int getIdentificationNumber() {
        return identificationNumber;
    }

    @Override
    public void start() {
        super.start();
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        status = Status.WORKING;
        while (true) {
            supplyRoutine();
            try {
                Thread.sleep(supplyInterval_ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
