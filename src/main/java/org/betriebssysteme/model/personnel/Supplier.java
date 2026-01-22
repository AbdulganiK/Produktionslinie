package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.stations.MainDepot;
import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Supplier extends Thread implements Personnel {
    private int identificationNumber;
    private Status status;
    private Task task;
    private int mainDepotId;
    private int originStationId;
    private int destinationStationId;
    private int supplyInterval_ms;
    private int supplyTimer_ms;
    private int travelTimer_ms;
    private Logger logger;
    private int idOfCurrentDestinationStation;
    private boolean ready = false;
    private HashMap<Cargo, Integer> cargoStorage = new HashMap<>();
    private int maxCapacity;

    public Supplier(int identificationNumber,
                    int supplyInterval_ms,
                    int supplyTimer_ms,
                    int travelTimer_ms,
                    int mainDepotId,
                    int maxCapacity) {
        this.identificationNumber = identificationNumber;
        this.mainDepotId = mainDepotId;
        this.supplyInterval_ms = supplyInterval_ms;
        this.supplyTimer_ms = supplyTimer_ms;
        this.travelTimer_ms = travelTimer_ms;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.status = StatusWarning.STOPPED;
        this.task = Task.JOBLESS;
        this.maxCapacity = maxCapacity;
        this.logger = org.slf4j.LoggerFactory.getLogger("Supplier-" + identificationNumber);
        logger.info("Supplier " + identificationNumber + " created with supply interval: " + supplyInterval_ms + " ms, supply timer: " + supplyTimer_ms + " ms.");
        this.cargoStorage = new HashMap<>();
    }

    private void supplyRoutine() throws InterruptedException {
        // Initialize Supplier cargo storage
        int cargoCapacityPerMaterial = maxCapacity / Material.values().length;
        for (Material material : Material.values()) {
            cargoStorage.put(material, cargoCapacityPerMaterial);
        }
        for (Product product : Product.values()) {
            cargoStorage.put(product, 0);
        }

        task = Task.DELIVERING;
        destinationStationId = mainDepotId;
        idOfCurrentDestinationStation = mainDepotId;
        logger.info("Supplier starting supply routine to Main Depot");

        awaitReady();
        // TODO: Implement ready check if needed

        refillDepotAndCollectCargo();
        task = Task.TRANSPORTING;
        originStationId = mainDepotId;
        destinationStationId = -1;
        idOfCurrentDestinationStation = -1;

        awaitReady();
        //TODO: Implement ready check if needed

        logger.info("Supplier finishing supply routine to Main Depot");
    }

    private void refillDepotAndCollectCargo() {
        try {
            Thread.sleep(supplyTimer_ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // TODO Implement the depot refilling logic
        for (Material material : Material.values()) {
            int currentQuantity = cargoStorage.get(material);
            int resizedQuantity = refillCargo(material, currentQuantity);
            cargoStorage.put(material, currentQuantity - resizedQuantity);
        }
        int freeCapacity = maxCapacity - cargoStorage.values().stream().mapToInt(Integer::intValue).sum();
        int collectedQuantity = collectCargo(Product.PACKAGE, freeCapacity);
        freeCapacity -= collectedQuantity;
        collectedQuantity = collectCargo(Product.SCRAP, freeCapacity);
        freeCapacity -= collectedQuantity;
        if (freeCapacity > 0) {
            logger.info("Supplier has free capacity left after collecting cargo: " + freeCapacity);
            System.out.println("Supplier has free capacity left after collecting cargo");
        }
        else {
            logger.info("Supplier cargo storage is full after collecting cargo.");
            System.out.println("Supplier cargo storage is full after collecting cargo");
        }
    }

    private synchronized void awaitReady() throws InterruptedException {
        ready = false;
        while (!ready) {
            wait();
        }
    }

    //============================================================================
    // Methods of Personnel interface
    @Override
    public synchronized void setReady() {
        ready = true;
        notifyAll();
    }
    @Override
    public int refillCargo(Cargo cargo, int quantity) {
        MainDepot mainDepot = (MainDepot) ProductionHeadquarters.getInstance().getStations().get(mainDepotId);
        int resivedQuantity = mainDepot.resiveCargo(cargo, quantity);
        logger.info("Depot refilled with materials");
        return resivedQuantity;
    }

    @Override
    public int collectCargo(Cargo cargo, int quantity) {
        MainDepot mainDepot = (MainDepot) ProductionHeadquarters.getInstance().getStations().get(mainDepotId);
        int receivedQuantity = mainDepot.handOverCargo(cargo, quantity);
        logger.info("Collected cargo from depot");
        return receivedQuantity;
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

    @Override
    public String[][] getInfoArray() {
        String[][] infoArray = new String[8][2];

        infoArray[0][0] = "Supplier ID";
        infoArray[0][1] = String.valueOf(identificationNumber);

        infoArray[1][0] = "Status";
        infoArray[1][1] = status.toString();

        infoArray[2][0] = "Current Task";
        infoArray[2][1] = task.toString();

        infoArray[3][0] = "Origin ID";
        if (originStationId == -1) {
            infoArray[3][1] = "N/A";
        } else {
            infoArray[3][1] = String.valueOf(originStationId);
        }
        infoArray[4][0] = "Destination ID";
        if (destinationStationId == -1) {
            infoArray[4][1] = "N/A";
        } else {
            infoArray[4][1] = String.valueOf(destinationStationId);
        }

        infoArray[5][0] = "Supply Interval (ms)";
        infoArray[5][1] = String.valueOf(supplyInterval_ms);

        infoArray[6][0] = "Supply Timer (ms)";
        infoArray[6][1] = String.valueOf(supplyTimer_ms);
        return infoArray;
    }

    @Override
    public int getIdOfDestinationStation() {
        return destinationStationId;
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        status = StatusInfo.OPERATIONAL;
        while (true) {
            try {
                supplyRoutine();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(supplyInterval_ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
