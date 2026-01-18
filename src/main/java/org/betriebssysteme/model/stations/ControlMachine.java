package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;

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
                          int probilityOfDefectPercent,
                          int maschinePriority) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                nextMaschine,
                new java.util.HashMap<Cargo, Integer>() {{
                    put(productCargo, initialQuantityOfProduct);
                    put(Product.SCRAP, 0);
                }},
                productCargo,
                maschinePriority);
        this.probilityOfDefectPercent = probilityOfDefectPercent;
    }


    @Override
    protected void checkStorageStatus() {
        try {
            logger.info("Checking storage status of ControlMachine " + identificationNumber);
            storageSemaphore.acquire();
            // Check product storage
            int productStorage = storage.getOrDefault(productCargo, 0);
            if (productStorage >= maxStorageCapacity) {
                if (status != StatusWarning.EMPTY) {
                    status = StatusWarning.FULL;
                    logger.info("Product storage is FULL in ControlMachine " + identificationNumber);
                }
            } else if (productStorage >= maxStorageCapacity * 0.25 && productStorage != 0) {
                if (status != StatusWarning.EMPTY) {
                    status = StatusCritical.LOW_CAPACITY;
                    logger.info("Product storage is LOW_CAPACITY in ControlMachine " + identificationNumber);
                }
            }
            else if (productStorage == 0) {
                if (status != StatusWarning.EMPTY) {
                    status = StatusWarning.EMPTY;
                    logger.info("Product storage is EMPTY in ControlMachine " + identificationNumber);
                }
            }
            // Check SCRAP storage
            int scrapStorage = storage.getOrDefault(Product.SCRAP, 0);
            if (scrapStorage >= maxStorageCapacity) {
                if (status != StatusWarning.FULL) {
                    status = StatusWarning.FULL;
                    logger.info("SCRAP storage is FULL in ControlMachine " + identificationNumber);
                }
                sendCargoRequest(Product.SCRAP, scrapStorage);
            }
            else if (scrapStorage >= maxStorageCapacity * 0.75) {
                if (status != StatusCritical.LOW_CAPACITY) {
                    status = StatusCritical.LOW_CAPACITY;
                    logger.info("SCRAP storage is LOW_CAPACITY in ControlMachine " + identificationNumber);
                }
                sendCargoRequest(Product.SCRAP, scrapStorage);
            }
            if (status != StatusWarning.FULL && status != StatusCritical.LOW_CAPACITY && status != StatusWarning.EMPTY) {
                status = StatusInfo.OPPERATIONAL;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
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
                System.out.println("ControlMachine " + identificationNumber + " started as all conditions are met");
            } else if (!productionPossible && running == true) {
                stopMachine();
                System.out.println("ControlMachine " + identificationNumber + " stopped due to insufficient storage conditions");
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
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
