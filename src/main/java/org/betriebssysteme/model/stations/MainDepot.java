package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.cargo.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;
import org.slf4j.Logger;

public class MainDepot implements Station {
    /**
     * The cargoStorage map represents the storage of the main depot.
     * The key is the Cargo type, and the value is the quantity of that cargo currently stored.
     * It is protected by semaphore
     * to ensure thread safety when multiple threads are accessing or modifying the storage concurrently.
     * The semaphore allows only one thread to access the cargoStorage at a time, preventing data corruption.
     */
    private final Map <Cargo, Integer> cargoStorage;
    private final Semaphore cargoStorageSemaphore;
    private final int identificationNumber;
    private final int maxStorageCapacity;
    private Status status;
    private final Logger logger;
    private final int initialStorageCapacity;

    /**
     * Constructor for the MainDepot class.
     * @param identificationNumber The identification number for the main depot.
     * @param maxStorageCapacity The maximum storage capacity for the main depot.
     * @param initialStorageCapacity The initial quantity of each material in storage when the main depot is created.
     */
    public MainDepot (int identificationNumber, int maxStorageCapacity, int initialStorageCapacity) {
        this.cargoStorage = new HashMap<>();
        this.identificationNumber = identificationNumber;
        this.maxStorageCapacity = maxStorageCapacity;
        this.cargoStorageSemaphore = new Semaphore(1);
        this.status = StatusWarning.STOPPED;
        this.logger = org.slf4j.LoggerFactory.getLogger("MainDepot-" + identificationNumber);
        logger.info("Main Depot {} created with max storage capacity of {}", identificationNumber, maxStorageCapacity);
        this.initialStorageCapacity = initialStorageCapacity;
        createInitialStorage();
    }

    /**
     * This method initializes the cargo storage of the main depot with the initial quantity of each material.
     */
    private void createInitialStorage() {
        for (Material material : Material.values()) {
            cargoStorage.put(material, initialStorageCapacity);
        }
    }

    /**
     * The method checkAndUpdateStatus checks the current quantities of cargo in storage
     * and updates the status of the main depot.
     * The status is updated based on the rules:
     * - a material cargo = 0 --> EMPTY
     * - a material cargo < 25% of the maximum storage capacity --> LOW_CAPACITY
     * - a product cargo >= maximum storage capacity --> FULL
     * - a product cargo > 75% of the maximum storage capacity --> LOW_CAPACITY
     */
    private void checkAndUpdateStatus() {
        status = StatusInfo.OPERATIONAL;
        for (Cargo cargo : cargoStorage.keySet()) {
            int quantity = cargoStorage.get(cargo);
            if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
                if (quantity == 0) {
                    if (status != StatusWarning.EMPTY){
                        status = StatusWarning.EMPTY;
                        logger.info("Status set to EMPTY due to {}", cargo);
                    }
                } else if (quantity < maxStorageCapacity * 0.25) {
                    if (status != StatusWarning.EMPTY && status != StatusCritical.LOW_CAPACITY){
                        status = StatusCritical.LOW_CAPACITY;
                        logger.info("Status set to CRITICAL due to {}", cargo);
                    }
                }
            } else if (cargo.getCargoTyp() == CargoTyp.PRODUCT) {
                if (quantity >= maxStorageCapacity) {
                    if (status != StatusWarning.FULL){
                        status = StatusWarning.FULL;
                        logger.info("Status set to FULL due to {}", cargo);
                    }
                } else if (quantity > maxStorageCapacity * 0.75) {
                    if (status != StatusWarning.FULL && status != StatusCritical.LOW_CAPACITY)
                        status = StatusCritical.LOW_CAPACITY;
                    logger.info("Status set to CRITICAL due to {}", cargo);
                }
            }
        }
    }

    /**
     * This method allows the main depot to receive cargo.
     * The method is synchronized
     * using semaphore to ensure that only one thread can access the cargo storage at a time,
     * preventing data corruption.
     * @param cargo The cargo to be received.
     * @param quantity The quantity of cargo to be received.
     * @return the quantity of cargo that was stored in the main depot
     */
    public int resiveCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            if (currentQuantity + quantity <= maxStorageCapacity) {
                cargoStorage.put(cargo, currentQuantity + quantity);
                checkAndUpdateStatus();
                logger.info("MainDepot {} accepted {} of {}", identificationNumber, quantity, cargo);
                return quantity;
            } else {
                int acceptedQuantity = maxStorageCapacity - currentQuantity;
                cargoStorage.put(cargo, maxStorageCapacity);
                checkAndUpdateStatus();
                logger.info("MainDepot {} accepted only {} of {} requested: {}", identificationNumber, acceptedQuantity, cargo, quantity);
                return acceptedQuantity;
            }
        } catch (Exception e) {
            return 0;
        }
        finally {
            logger.info("Received {} of {}", quantity, cargo);
            cargoStorageSemaphore.release();
        }
    }

    /**
     * This method allows the main depot to hand over cargo.
     * The method is synchronized
     * using semaphore to ensure that only one thread can access the cargo storage at a time,
     * preventing data corruption.
     * @param cargo The cargo to be handed over.
     * @param quantity The quantity of cargo to be handed over.
     * @return the quantity of cargo that was handed over by the main depot
     */
    public int handOverCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            if (currentQuantity >= quantity) {
                cargoStorage.put(cargo, currentQuantity - quantity);
                checkAndUpdateStatus();
                logger.info("MainDepot {} handed over {} of {}", identificationNumber, quantity, cargo);
                return quantity;
            } else {
                cargoStorage.put(cargo, 0);
                checkAndUpdateStatus();
                logger.info("MainDepot {} handed over only {} of {} requested: {}", identificationNumber, currentQuantity, cargo, quantity);
                return currentQuantity;
            }
        } catch (Exception e) {
            return 0;
        }
        finally {
            logger.info("Handed over {} of {}", quantity, cargo);
            cargoStorageSemaphore.release();
        }
    }

    //============================================================================
    // Getters and Setters
    public Status getStatus() {
        return status;
    }


    public int getIdentificationNumber() {
        return identificationNumber;
    }

    //============================================================================
    @Override
    public void start() {
        // MainDepot does not have a separate thread of execution
    }

    @Override
    public String [][] getInfoArray(){
        String [][] infoArray = new String[cargoStorage.size() + 4][2];

        int index = 0;
        infoArray[index++] = new String[]{"ID", String.valueOf(identificationNumber)};
        infoArray[index++] = new String[]{"Max Capacity", String.valueOf(maxStorageCapacity)};
        infoArray[index++] = new String[]{"Current Status", String.valueOf(status)};
        infoArray[index++] = new String[]{"Cargo", "Quantity"};

        for (Map.Entry<Cargo, Integer> entry : cargoStorage.entrySet()){
            infoArray[index++] = new String[]{entry.getKey().toString(), String.valueOf(entry.getValue())};
        }
        return infoArray;
    }
}
