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
    private Map <Cargo, Integer> cargoStorage;
    private Semaphore cargoStorageSemaphore;
    private int identificationNumber;
    private int maxStorageCapacity;
    private Status status;
    private Logger logger;
    private int initialStorageCapacity;

    public MainDepot (int maxStorageCapacity, int initialStorageCapacity) {
        this.cargoStorage = new HashMap<Cargo, Integer>();
        this.identificationNumber = 1;
        this.maxStorageCapacity = maxStorageCapacity;
        this.cargoStorageSemaphore = new Semaphore(1);
        this.status = StatusWarning.STOPPED;
        this.logger = org.slf4j.LoggerFactory.getLogger("MainDepot-" + identificationNumber);
        logger.info("Main Depot " + identificationNumber + " created with max storage capacity of " + maxStorageCapacity);
        this.initialStorageCapacity = initialStorageCapacity;
        createInitialStorage();
    }

    private void createInitialStorage() {
        for (Material material : Material.values()) {
            cargoStorage.put(material, initialStorageCapacity);
        }
    }

    private void checkAndUpdateStatus() {
        status = StatusInfo.OPERATIONAL;
        for (Cargo cargo : cargoStorage.keySet()) {
            int quantity = cargoStorage.get(cargo);
            if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
                if (quantity == 0) {
                    if (status != StatusWarning.EMPTY){
                        status = StatusWarning.EMPTY;
                        logger.info("Status set to EMPTY due to " + cargo);
                    }
                } else if (quantity < maxStorageCapacity * 0.25) {
                    if (status != StatusWarning.EMPTY && status != StatusCritical.LOW_CAPACITY){
                        status = StatusCritical.LOW_CAPACITY;
                        logger.info("Status set to CRITICAL due to " + cargo);
                    }
                }
            } else if (cargo.getCargoTyp() == CargoTyp.PRODUCT) {
                if (quantity >= maxStorageCapacity) {
                    if (status != StatusWarning.FULL){
                        status = StatusWarning.FULL;
                        logger.info("Status set to FULL due to " + cargo);
                    }
                } else if (quantity > maxStorageCapacity * 0.75) {
                    if (status != StatusWarning.FULL && status != StatusCritical.LOW_CAPACITY)
                        status = StatusCritical.LOW_CAPACITY;
                        logger.info("Status set to CRITICAL due to " + cargo);
                }
            }
        }
    }

    public int getMaxStorageCapacity() {
        return maxStorageCapacity;
    }

    public int resiveCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            if (currentQuantity + quantity <= maxStorageCapacity) {
                cargoStorage.put(cargo, currentQuantity + quantity);
                checkAndUpdateStatus();
                logger.info("MainDepot " + identificationNumber + " accepted " + quantity + " of " + cargo);
                return quantity;
            } else {
                int acceptedQuantity = maxStorageCapacity - currentQuantity;
                cargoStorage.put(cargo, maxStorageCapacity);
                checkAndUpdateStatus();
                logger.info("MainDepot " + identificationNumber + " accepted only " + acceptedQuantity + " of " + cargo + " requested: " + quantity);
                return acceptedQuantity;
            }
        } catch (Exception e) {
            return 0;
        }
        finally {
            logger.info("Received " + quantity + " of " + cargo);
            cargoStorageSemaphore.release();
        }
    }

    public int handOverCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            if (currentQuantity >= quantity) {
                cargoStorage.put(cargo, currentQuantity - quantity);
                checkAndUpdateStatus();
                logger.info("MainDepot " + identificationNumber + " handed over " + quantity + " of " + cargo);
                return quantity;
            } else {
                cargoStorage.put(cargo, 0);
                checkAndUpdateStatus();
                logger.info("MainDepot " + identificationNumber + " handed over only " + currentQuantity + " of " + cargo + " requested: " + quantity);
                return currentQuantity;
            }
        } catch (Exception e) {
            return 0;
        }
        finally {
            logger.info("Handed over " + quantity + " of " + cargo);
            cargoStorageSemaphore.release();
        }
    }


    public Status getStatus() {
        return status;
    }


    public Map getInformationMap() {
        return cargoStorage;
    }


    public int getIdentificationNumber() {
        return identificationNumber;
    }

    @Override
    public void start() {
        // MainDepot does not have a separate thread of execution
    }

    @Override
    public String [][] getInfoArray(){
        String [][] infoArray = new String[cargoStorage.size()+4][2];

        infoArray[0][0] = "ID";
        infoArray[0][1] = Integer.toString(identificationNumber);

        infoArray[1][0] = "Max Capacity";
        infoArray[1][1] = Integer.toString(maxStorageCapacity);

        infoArray[2][0] = "Current Status";
        infoArray[2][1] = status.toString();

        infoArray[3][0] = "Cargo";
        infoArray[3][1] = "Quantity";

        int index = 4;
        for (Cargo cargo : cargoStorage.keySet()){
            infoArray[index][0] = cargo.toString();
            infoArray[index][1] = Integer.toString(cargoStorage.get(cargo));
            index++;
        }
        return infoArray;
    }
}
