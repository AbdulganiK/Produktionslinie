package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

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
    }

    private void runProductionCycle() {
        System.out.println("Machine " + identificationNumber + " starting production cycle.");
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        checkStorageStatus();
        checkIfCargoPrductionIsPossible();
        if (running){
            Cargo producedCargo = produceProduct();
            storePrductOrDeliverToNextMachine(producedCargo);
        }
    }

    protected abstract void checkStorageStatus();

    protected abstract void checkIfCargoPrductionIsPossible();

    protected abstract Cargo produceProduct();

    protected abstract void storePrductOrDeliverToNextMachine(Cargo cargo);

    protected void sendCargoRequest(Cargo cargo, int quantity) {
        if (!requestedCargoTypes.get(cargo)){
            Request request = new Request(quantity,1, cargo, this.identificationNumber);
            if (productionHeadquarters == null){
                System.out.println("Production headquarters is null in machine " + identificationNumber);
            }
            productionHeadquarters.addRequest(request);
        }
    }

    protected void deliverToNextMachine() {
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
            storageSemaphore.release();
        }
    }

    public void stopMachine() {
        System.out.println("Machine " + identificationNumber + " stopping.");
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
        System.out.println("Machine " + identificationNumber + " starting.");
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
