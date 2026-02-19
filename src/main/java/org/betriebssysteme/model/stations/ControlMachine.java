package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;

/**
 * The ControlMachine class represents a machine that controlled all handover products.
 * It's implements specific behavior for controlling production based on storage status and defect probability.
 */
public class ControlMachine extends Maschine{
    private final int probabilityOfDefectPercent;

    /**
     * Constructor for the ControlMachine class.
     * @param identificationNumber The identification number for the machine.
     * @param timeToSleep The time in milliseconds that the machine will sleep between production cycles.
     * @param maxStorageCapacity The maximum storage capacity for the machine.
     * @param initialQuantityOfProduct The initial quantity of the product in storage.
     * @param productCargo The type of product cargo that the machine produces.
     * @param nextMaschine The next machine in the production line to which the produced cargo will be delivered.
     * @param productionTime The time in milliseconds that the machine takes to check the product.
     * @param probabilityOfDefectPercent The probability (in percentage) that a produced product is defective.
     * @param maschinePriority The priority level of the machine for requests
     */
    public ControlMachine(int identificationNumber,
                          int timeToSleep,
                          int maxStorageCapacity,
                          int initialQuantityOfProduct,
                          Cargo productCargo,
                          Maschine nextMaschine,
                          int productionTime,
                          int probabilityOfDefectPercent,
                          int maschinePriority) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                nextMaschine,
                new java.util.HashMap<>() {{
                    put(productCargo, initialQuantityOfProduct);
                    put(Product.SCRAP, 0);
                }},
                productCargo,
                maschinePriority);
        this.probabilityOfDefectPercent = probabilityOfDefectPercent;
    }


    @Override
    protected void checkStorageStatus() {
        try {
            Status newStatus = StatusInfo.OPERATIONAL;
            logger.info("Checking storage status of ControlMachine {}", identificationNumber);
            storageSemaphore.acquire();
            // Check product storage
            int productStorage = storage.getOrDefault(productCargo, 0);
            if (productStorage >= maxStorageCapacity) {
                newStatus = StatusWarning.FULL;
                logger.info("Product storage is FULL in ControlMachine {}", identificationNumber);
            } else if (productStorage >= maxStorageCapacity * 0.25 && productStorage != 0) {
                newStatus = StatusCritical.LOW_CAPACITY;
                logger.info("Product storage is LOW_CAPACITY in ControlMachine {}", identificationNumber);
            }
            else if (productStorage == 0) {
                newStatus = StatusWarning.EMPTY;
                logger.info("Product storage is EMPTY in ControlMachine {}", identificationNumber);
            }
            // Check SCRAP storage
            int scrapStorage = storage.getOrDefault(Product.SCRAP, 0);
            if (scrapStorage >= maxStorageCapacity) {
                if (newStatus != StatusWarning.FULL) {
                    newStatus = StatusWarning.FULL;
                    logger.info("SCRAP storage is FULL in ControlMachine {}", identificationNumber);
                }
                sendCargoRequest(Product.SCRAP, scrapStorage);
            }
            else if (scrapStorage >= maxStorageCapacity * 0.75) {
                if (newStatus != StatusCritical.LOW_CAPACITY) {
                    newStatus = StatusCritical.LOW_CAPACITY;
                    logger.info("SCRAP storage is LOW_CAPACITY in ControlMachine {}", identificationNumber);
                }
                sendCargoRequest(Product.SCRAP, scrapStorage);
            }
            status = newStatus;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected void checkIfCargoProductionIsPossible() {
        try {
            boolean productionPossible = true;
            storageSemaphore.acquire();
            int currentStorage = storage.getOrDefault(productCargo, 0);
            int capacityLeftForScrap = maxStorageCapacity - storage.getOrDefault( Product.SCRAP, 0);
            if (currentStorage < 1 || capacityLeftForScrap < 1) {
                productionPossible = false;
            }
            if (productionPossible && !running){
                startMachine();
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled())
                {
                    System.out.println("ControlMachine " + identificationNumber + " started as all conditions are met");
                }
            } else if (!productionPossible && running) {
                stopMachine();
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("ControlMachine " + identificationNumber + " stopped as conditions are not met");
                }
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
        if (randomValue < probabilityOfDefectPercent) {
            logger.info("ControlMachine {} produced a DEFECT product.", identificationNumber);
            return Product.SCRAP;
        }
        logger.info("ControlMachine {} produced a GOOD product.", identificationNumber);
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return productCargo;
    }

    @Override
    protected void storeProductOrDeliverToNextMachine(Cargo cargo) {
        if (cargo == Product.SCRAP) {
            storeProduct(cargo);
            logger.info("ControlMachine {} storing DEFECT product: {}", identificationNumber, cargo);
        } else if (nextMaschine != null) {
            logger.info("ControlMachine {} delivering product to next machine: {}", identificationNumber, cargo);
            deliverToNextMachine(cargo);
        }
        else{
            logger.info("ControlMachine {} not storing product: {}", identificationNumber, cargo);
        }
    }
}
