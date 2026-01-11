package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Product;

import java.util.Map;

public class ControlMachine extends Maschine{
    private int probilityOfDefectPercent;


    public ControlMachine(int identificationNumber,
                          int timeToSleep,
                          int maxStorageCapacity,
                          int initialQuantityOfProduct,
                          Cargo productCargo,
                          Maschine nextMaschine,
                          int productionTime,
                          ProductionHeadquarters productionHeadquarters,
                          int probilityOfDefectPercent) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                productionHeadquarters,
                nextMaschine,
                new java.util.HashMap<Cargo, Integer>() {{
                    put(productCargo, initialQuantityOfProduct);
                    put(Product.SCRAP, 0);
                }},
                productCargo);
        this.probilityOfDefectPercent = probilityOfDefectPercent;
    }


    @Override
    protected void checkStorageStatus() {
        try {
            logger.info("Checking storage status of ControlMachine " + identificationNumber);
            storageSemaphore.acquire();
            int productStorage = storage.getOrDefault(productCargo, 0);
            if (productStorage >= maxStorageCapacity) {
                status = org.betriebssysteme.model.Status.FULL;
                return;
            } else if (productStorage >= maxStorageCapacity * 0.25) {
                status = org.betriebssysteme.model.Status.LOW_CAPACITY;
                return;
            } else {
                status = org.betriebssysteme.model.Status.OPERATING;
            }
            int scrapStorage = storage.getOrDefault(Product.SCRAP, 0);
            if (scrapStorage >= maxStorageCapacity) {
                status = org.betriebssysteme.model.Status.FULL;
                sendCargoRequest(Product.SCRAP, scrapStorage);
                return;
            }
            else if (scrapStorage >= maxStorageCapacity * 0.75) {
                status = org.betriebssysteme.model.Status.LOW_CAPACITY;
                sendCargoRequest(Product.SCRAP, scrapStorage);
                return;
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected void checkIfCargoPrductionIsPossible() {
        try {
            boolean productionPossible = true;
            storageSemaphore.acquire();
            int currentStorage = storage.getOrDefault(productCargo, 0);
            int capacityLeftForScrap = maxStorageCapacity - storage.getOrDefault( Product.SCRAP, 0);
            if (currentStorage < 1 || capacityLeftForScrap < 1) {
                productionPossible = false;
            }
            if (productionPossible && running == false){
                startMachine();
            } else if (!productionPossible && running == true) {
                stopMachine();
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected Cargo produceProduct() {
        int randomValue = (int) (Math.random() * 100);
        if (randomValue < probilityOfDefectPercent) {
            logger.info("ControlMachine " + identificationNumber + " produced a DEFECT product.");
            return Product.SCRAP;
        }
        logger.info("ControlMachine " + identificationNumber + " produced a GOOD product.");
        return productCargo;
    }

    @Override
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        if (cargo == Product.SCRAP) {
            storeProduct(cargo);
            logger.info("ControlMachine " + identificationNumber + " storing DEFECT product: " + cargo);
        } else if (nextMaschine != null) {
            logger.info("ControlMachine " + identificationNumber + " delivering product to next machine: " + cargo);
            deliverToNextMachine(cargo);
        }
        else{
            logger.info("ControlMachine " + identificationNumber + " not storing product: " + cargo);
        }
    }
}
