package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Maschine extends Thread implements Station{
    protected int identificationNumber;
    protected Maschine nextMaschine;
    protected int timeToProcess;
    protected int timeToSleep;
    protected boolean running;
    protected ProductionHeadquarters productionHeadquarters;
    protected Status status;
    protected int maxStorageCapacity;
    protected Semaphore storageSemaphore;
    protected Map<Cargo, Integer> storage;
    protected Map<Cargo, Boolean> requestedCargoTypes;
    protected Cargo productCargo;
    protected Logger logger;

    public Maschine(int identificationNumber,
                    int timeToProcess,
                    int timeToSleep,
                    int maxStorageCapacity,
                    ProductionHeadquarters productionHeadquarters,
                    Maschine nextMaschine,
                    Map<Cargo, Integer> initialStorage,
                    Cargo productCargo) {
        this.identificationNumber = identificationNumber;
        this.timeToProcess = timeToProcess;
        this.timeToSleep = timeToSleep;
        this.maxStorageCapacity = maxStorageCapacity;
        this.productionHeadquarters = productionHeadquarters;
        this.nextMaschine = nextMaschine;
        this.storage = initialStorage;
        this.storageSemaphore = new Semaphore(1);
        this.status = Status.OPERATING;
        this.running = true;
        this.productCargo = productCargo;
        this.requestedCargoTypes = new HashMap<Cargo, Boolean>();
        this.logger = LoggerFactory.getLogger("Maschine-" + identificationNumber);
        logger.info("Maschine " + identificationNumber + " initialized for product: " + productCargo);
    }

    private void runProductionCycle() {
        logger.info("Starting production cycle");
        checkStorageStatus();
        checkIfCargoPrductionIsPossible();
        if (running == true){
            Cargo producedCargo = produceProduct();
            storePrductOrDeliverToNextMachine(producedCargo);
        }
        else{
            try {
                Thread.sleep(timeToSleep);
                // Release CPU for a while when not running for better performance when stopped
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract void checkStorageStatus();

    protected abstract void checkIfCargoPrductionIsPossible();

    protected abstract Cargo produceProduct();

    protected abstract void storePrductOrDeliverToNextMachine(Cargo cargo);

    protected void sendCargoRequest(Cargo cargo, int quantity) {
        boolean requestedBefore = requestedCargoTypes.getOrDefault(cargo, false);
        if (requestedBefore == false){
            Request request = new Request(quantity,1, cargo, this.identificationNumber);
            if (productionHeadquarters == null){
                logger.error("No production headquarters available");
            }
            System.out.println("Machine " + identificationNumber + " sending request for cargo: " + cargo + " quantity: " + quantity);
            productionHeadquarters.addRequest(request);
            logger.info("Added request to headquarters for cargo: " + cargo + " quantity: " + quantity);
        }
    }

    public void markRequestAsCompleted(Cargo cargo){
        System.out.println("Machine " + identificationNumber + " marked request as completed for cargo: " + cargo);
        requestedCargoTypes.put(cargo, false);
    }

    protected void deliverToNextMachine(Cargo cargo) {
        if (nextMaschine != null) {
            boolean cargoDelivered = false;
            while (!cargoDelivered) {
                try {
                    logger.info("Trying to deliver product to next machine: " + nextMaschine.getIdentificationNumber());
                    int deliveredQuantity = nextMaschine.resiveCargo(cargo, 1);
                    if (deliveredQuantity == 0) {
                        if (running){
                            System.out.println("Machine " + identificationNumber + " stopping due to next machine " + nextMaschine.getIdentificationNumber() + " storage full.");
                            stopMachine();
                        }
                        logger.info("Next machine storage full, retrying in 500ms");
                        Thread.sleep(500);
                    }
                    else {
                        if (!running){
                            startMachine();
                        }
                        cargoDelivered = true;
                        logger.info("Product delivered to next machine: " + nextMaschine.getIdentificationNumber());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Machine " + identificationNumber + " interrupted while delivering cargo", e);
                }
            }
            logger.info("Delivering product to next machine: " + nextMaschine.getIdentificationNumber());
            nextMaschine.resiveCargo(cargo, 1);
        } else {
            logger.warn("Next machine is null, cannot deliver product");
        }
    }

    protected void storeProduct(Cargo cargo) {
        try {
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity < maxStorageCapacity) {
                    storage.put(cargo, currentQuantity + 1);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Stored product in machine storage: " + cargo);
            storageSemaphore.release();
        }
    }

    public void stopMachine() {
        running = false;
        logger.debug("Stopping machine" + identificationNumber);
        System.out.println("Machine " + identificationNumber + " stopped.");
    }

    public void startMachine() {
        running = true;
        logger.debug("Starting machine" + identificationNumber);
        System.out.println("------------------- Machine " + identificationNumber + " started.");
    }

    public boolean isRunning() {
        return running;
    }

    public void setNextMaschine(Maschine nextMaschine) {
        this.nextMaschine = nextMaschine;
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        logger.info("Starting thread");
        while (true) {
            runProductionCycle();
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ============================================================================
    //Station methods
    @Override
    public int resiveCargo(Cargo cargo, int quantity) {
        try{
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity + quantity <= maxStorageCapacity) {
                    storage.put(cargo, currentQuantity + quantity);
                    return quantity;
                } else {
                    int acceptedQuantity = maxStorageCapacity - currentQuantity;
                    storage.put(cargo, maxStorageCapacity);
                    return acceptedQuantity;
                }
            }
            else {
                return 0;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Machine "+ identificationNumber + " interrupted while receiving cargo", e);
        } finally {
            logger.info("Stored product in machine storage: " + cargo);
            storageSemaphore.release();
        }
    }

    @Override
    public int handOverCargo(Cargo cargo, int quantity) {
        try{
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity >= quantity) {
                    storage.put(cargo, currentQuantity - quantity);
                    return quantity;
                } else {
                    storage.put(cargo, 0);
                    return currentQuantity;
                }
            }
            else {
                logger.debug("Requested cargo not available in storage: " + cargo);
                return 0;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Handed over product from machine storage: " + cargo + " quantity: " + quantity);
            storageSemaphore.release();
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Map getInformationMap() {
        return storage;
    }

    @Override
    public int getIdentificationNumber() {
        return identificationNumber;
    }

    @Override
    public void start() {
        super.start();
    }

    public void setProductionHeadquarters(ProductionHeadquarters productionHeadquarters){
        this.productionHeadquarters = productionHeadquarters;
    }

    @Override
    public String [][] getInfoArray(){
        String [][] infoArray = new String[storage.size()+8][2];
        infoArray[0][0] = "Maschine ID";
        infoArray[0][1] = Integer.toString(identificationNumber);

        infoArray[1][0] = "Status";
        infoArray[1][1] = status.toString();

        infoArray[2][0] = "Running";
        infoArray[2][1] = Boolean.toString(running);

        infoArray[3][0] = "Max Storage Capacity";
        infoArray[3][1] = Integer.toString(maxStorageCapacity);

        infoArray[4][0] = "Product Cargo";
        infoArray[4][1] = productCargo.toString();

        infoArray [5][0] = "Next Maschine ID";
        if (nextMaschine != null){
            infoArray[5][1] = Integer.toString(nextMaschine.getIdentificationNumber());
        }
        else{
            infoArray[5][1] = "None";
        }

        infoArray [6][0] = "Time To Process (ms)";
        infoArray[6][1] = Integer.toString(timeToProcess);

        infoArray [7][0] = "Storage";
        infoArray[7][1] = "Quantity";

        int index = 8;
        for (Map.Entry<Cargo, Integer> entry : storage.entrySet()) {
            infoArray[index][0] = entry.getKey().toString();
            infoArray[index][1] = Integer.toString(entry.getValue());
            index++;
        }
        return infoArray;
    }
}
