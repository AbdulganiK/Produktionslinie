package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class MainDepot implements Station{
    private Map <Cargo, Integer> cargoStorage;
    public Semaphore cargoStorageSemaphore;
    private int identificationNumber;
    private int maxStorageCapacity;
    private Status status;

    MainDepot (int maxStorageCapacity) {
        this.cargoStorage = new HashMap<Cargo, Integer>();
        this.identificationNumber = 1;
        this.maxStorageCapacity = maxStorageCapacity;
        this.cargoStorageSemaphore = new Semaphore(1);
        this.status = Status.OPERATING;
    }

    private void checkAndUpdateStatus() {
        status = Status.OPERATING;
        for (Cargo cargo : cargoStorage.keySet()) {
            int quantity = cargoStorage.get(cargo);
            if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
                if (quantity == 0) {
                    status = Status.EMPTY;
                } else if (quantity < maxStorageCapacity * 0.25) {
                    status = Status.LOW_CAPACITY;
                }
            } else if (cargo.getCargoTyp() == CargoTyp.PRODUCT) {
                if (quantity >= maxStorageCapacity) {
                    status = Status.FULL;
                } else if (quantity > maxStorageCapacity * 0.75) {
                    status = Status.LOW_CAPACITY;
                }
            }
        }
    }

    public int getMaxStorageCapacity() {
        return maxStorageCapacity;
    }


    // ============================================================================
    //Station methods

    @Override
    public int resiveCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            int totalQuantity = currentQuantity + quantity;
            if (totalQuantity <= maxStorageCapacity) {
                cargoStorage.put(cargo, totalQuantity);
                checkAndUpdateStatus();
                return 0;
            } else {
                cargoStorage.put(cargo, maxStorageCapacity);
                checkAndUpdateStatus();
                return totalQuantity - maxStorageCapacity;
            }
        } catch (Exception e) {
            return quantity;
        }
        finally {
            cargoStorageSemaphore.release();
        }
    }

    @Override
    public int handOverCargo(Cargo cargo, int quantity) {
        try {
            cargoStorageSemaphore.acquire();
            int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
            if (currentQuantity >= quantity) {
                cargoStorage.put(cargo, currentQuantity - quantity);
                checkAndUpdateStatus();
                return quantity;
            } else {
                cargoStorage.put(cargo, 0);
                checkAndUpdateStatus();
                return currentQuantity;
            }
        } catch (Exception e) {
            return quantity;
        }
        finally {
            cargoStorageSemaphore.release();
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Map getInformationMap() {
        return cargoStorage;
    }

    @Override
    public int getIdentificationNumber() {
        return identificationNumber;
    }
}
