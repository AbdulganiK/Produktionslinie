package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;

import java.util.Map;
import java.util.concurrent.Semaphore;

public abstract class Maschine extends Thread implements Station{
    private int identificationNumber;
    private Maschine nextMaschine;
    private int timeToProcess;
    private int timeToSleep;
    private boolean running;
    private ProductionHeadquarters productionHeadquarters;
    private Status status;
    private int maxStorageCapacity;
    private Semaphore storageSemaphore;
    private Map<Cargo, Integer> storage;
    private Map<CargoTyp, Boolean> requestedCargoTypes;
    private Cargo productCargo;

    public Maschine(int identificationNumber, int timeToProcess, int timeToSleep, int maxStorageCapacity, ProductionHeadquarters productionHeadquarters, Maschine nextMaschine, Map<Cargo, Integer> initialStorage) {
        this.identificationNumber = identificationNumber;
        this.timeToProcess = timeToProcess;
        this.timeToSleep = timeToSleep;
        this.maxStorageCapacity = maxStorageCapacity;
        this.productionHeadquarters = productionHeadquarters;
        this.nextMaschine = nextMaschine;
        this.storage = initialStorage;
        this.storageSemaphore = new Semaphore(1);
        this.status = Status.OPERATING;
        this.running = false;
    }








    private void runProductionCycle() {
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Cargo producedCargo = produceProduct();
        storeProduct(producedCargo);
        deliverToNextMachine();
    }

    protected abstract void checkStorageStatus();

    private void sendCargoRequest(Cargo cargo, int currentQuantity) {
        if (cargo.getCargoTyp() == CargoTyp.MATERIAL && !requestedCargoTypes.getOrDefault(cargo, true)) {
            Request request = new Request(maxStorageCapacity - currentQuantity, 1, cargo, this.identificationNumber);
            productionHeadquarters.addRequest(request);
        } else if (cargo.getCargoTyp()== CargoTyp.PRODUCT && !requestedCargoTypes.getOrDefault(cargo, true)) {
            Request request = new Request(currentQuantity, 1, cargo, this.identificationNumber);
            productionHeadquarters.addRequest(request);
        }
        // TODO: Implement priority handling
    }

    protected abstract Cargo produceProduct();

    private void deliverToNextMachine() {
        if (nextMaschine != null) {
            try {
                storageSemaphore.acquire();
                int availableQuantity = storage.getOrDefault(productCargo, 0);
                int handedOverQuantity = nextMaschine.resiveCargo(productCargo, availableQuantity);
                storage.put(productCargo, availableQuantity - handedOverQuantity);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            finally {
                storageSemaphore.release();
            }
        }
    }

    private void storeProduct(Cargo cargo) {
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
            storageSemaphore.release();
        }
    }

    public void stopMachine() {
        running = false;
    }

    public void startMachine() {
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        while (running) {
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
            throw new RuntimeException(e);
        } finally {
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
                return 0;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
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
}
