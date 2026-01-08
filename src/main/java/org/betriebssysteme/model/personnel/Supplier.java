package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.stations.MainDepot;

import java.util.Map;

public class Supplier extends Thread implements Personnel {
    private int identificationNumber;
    private Status status;
    private Task task;
    private MainDepot mainDepot;
    private int originStationId;
    private int destinationStationId;
    private int supplyInterval;
    private int supplyTimer;
    private int travelTimer;

    public Supplier(int identificationNumber, MainDepot mainDepot, int supplyInterval, int supplyTimer, int travelTimer) {
        this.identificationNumber = identificationNumber;
        this.mainDepot = mainDepot;
        this.supplyInterval = supplyInterval;
        this.supplyTimer = supplyTimer;
        this.travelTimer = travelTimer;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.status = Status.STOPPED;
        this.task = Task.JOBLESS;
    }

    private void supplyRoutine() {
        task = Task.DELIVERING;
        destinationStationId = mainDepot.getIdentificationNumber();
        try {
            Thread.sleep(travelTimer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        refillDepotAndCollectCargo();
        task = Task.TRANSPORTING;
        originStationId = mainDepot.getIdentificationNumber();
        destinationStationId = -1;
        try {
            Thread.sleep(travelTimer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        originStationId = -1;
    }

    private void refillDepotAndCollectCargo() {
        try {
            Thread.sleep(supplyTimer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // TODO Implement the depot refilling logic
    }

    // ============================================================================
    //Personnel methods
    @Override
    public int refillCargo(Cargo cargo, int quantity) {
        for (Material material : Material.values()) {
            mainDepot.resiveCargo(material, mainDepot.getMaxStorageCapacity());
        }
        return 0;
    }

    @Override
    public int collectCargo(Cargo cargo, int quantity) {
        mainDepot.handOverCargo(Product.SCRAP, mainDepot.getMaxStorageCapacity());
        mainDepot.handOverCargo(Product.SHIPPING_PACKAGE, mainDepot.getMaxStorageCapacity());
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
                Thread.sleep(supplyInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
