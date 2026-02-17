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
    private final int identificationNumber;
    private Status status;
    private Task task;
    private final int mainDepotId;
    private final int supplyInterval_ms;
    private final int supplyTimer_ms;
    private final Logger logger;
    private int idOfCurrentDestinationStation;
    private boolean ready = false;
    private final HashMap<Cargo, Integer> cargoStorage;
    private final int maxCapacity;

    public Supplier(int identificationNumber,
                    int supplyInterval_ms,
                    int supplyTimer_ms,
                    int mainDepotId,
                    int maxCapacity) {
        this.identificationNumber = identificationNumber;
        this.mainDepotId = mainDepotId;
        this.supplyInterval_ms = supplyInterval_ms;
        this.supplyTimer_ms = supplyTimer_ms;
        this.status = StatusWarning.STOPPED;
        this.task = Task.JOBLESS;
        this.maxCapacity = maxCapacity;
        this.logger = org.slf4j.LoggerFactory.getLogger("Supplier-" + identificationNumber);
        logger.info("Supplier {} created with supply interval: {} ms, supply timer: {} ms.", identificationNumber, supplyInterval_ms, supplyTimer_ms);
        this.cargoStorage = new HashMap<>();
        setDaemon(true);
    }

    private void supplyRoutine() throws InterruptedException {
        // Initialize Supplier cargo storage
        int cargoCapacityPerMaterial = maxCapacity / Material.values().length;
        for (Material material : Material.values()) {
            cargoStorage.put(material, cargoCapacityPerMaterial);
        }
        cargoStorage.put(Product.SCRAP, 0);
        cargoStorage.put(Product.PACKAGE, 0);

        task = Task.DELIVERING;
        idOfCurrentDestinationStation = mainDepotId;
        logger.info("Supplier starting supply routine to Main Depot");
        awaitReady();
        refillDepotAndCollectCargo();
        task = Task.TRANSPORTING;
        idOfCurrentDestinationStation = -1;
        awaitReady();
        logger.info("Supplier finishing supply routine to Main Depot");
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("Supplier " + identificationNumber + " finished supply routine to Main Depot");
        }
    }

    private void refillDepotAndCollectCargo() {
        try {
            Thread.sleep(supplyTimer_ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
            logger.info("Supplier has free capacity left after collecting cargo: {}", freeCapacity);
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("Supplier has free capacity left after collecting cargo");
            }
        }
        else {
            logger.info("Supplier cargo storage is full after collecting cargo.");
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("Supplier cargo storage is full after collecting cargo");
            }
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
    public int getIdentificationNumber() {
        return identificationNumber;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public String[][] getInfoArray() {
        String[][] infoArray = new String[7 + cargoStorage.size()][2];

        int index = 0;
        infoArray[index++] = new String[]{"Supplier ID", String.valueOf(identificationNumber)};
        infoArray[index++] = new String[]{"Status", String.valueOf(status)};
        infoArray[index++] = new String[]{"Current Task", String.valueOf(task)};
        infoArray[index++] = new String[]{"Supply Interval (ms)", String.valueOf(supplyInterval_ms)};
        infoArray[index++] = new String[]{"Supply Timer (ms)", String.valueOf(supplyTimer_ms)};
        infoArray[index++] = new String[]{"Cargo Storage", "Quantity"};

        for (Map.Entry<Cargo, Integer> entry : cargoStorage.entrySet()){
            infoArray[index++] = new String[]{entry.getKey().toString(), String.valueOf(entry.getValue())};
        }
        return infoArray;
    }

    @Override
    public int getIdOfDestinationStation() {
        return idOfCurrentDestinationStation;
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        status = StatusInfo.OPERATIONAL;
        while (true) {
            try {
                supplyRoutine();
                Thread.sleep(supplyInterval_ms);
            } catch (InterruptedException e) {
                logger.info("Supplier {} interrupted, shutting down", identificationNumber);
                status = StatusWarning.STOPPED;
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
