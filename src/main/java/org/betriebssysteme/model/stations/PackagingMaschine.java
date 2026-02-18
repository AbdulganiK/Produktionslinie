package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;

/**
 * The PackagingMaschine class represents a machine responsible for packaging products based on a given recipe.
 * It extends the Maschine class and implements specific behavior for checking storage status, determining if production is possible,
 * producing products, and storing or delivering products to the next machine in the production line.
 */
public class PackagingMaschine extends Maschine {
    private final Recipe recipe;

    /**
     * Constructor for PackagingMaschine.
     *
     * @param identificationNumber   Unique identifier for the machine.
     * @param timeToSleep           Time the machine sleeps between checks (in milliseconds).
     * @param maxStorageCapacity    Maximum storage capacity of the machine.
     * @param nextMaschine          The next machine in the production line.
     * @param initialQuantityOfProduct Initial quantity of the product in storage.
     * @param productionTime        Time taken to produce one unit of product (in milliseconds).
     * @param recipe                The recipe that defines the ingredients and their quantities for production.
     * @param maschinePriority      The priority of the machine in the production line.
     */
    public PackagingMaschine(int identificationNumber,
                              int timeToSleep,
                              int maxStorageCapacity,
                              Maschine nextMaschine,
                              int initialQuantityOfProduct,
                              int productionTime,
                              Recipe recipe,
                              int maschinePriority) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                nextMaschine,
                recipe.getInitalStorageWithProduct(initialQuantityOfProduct),
                Product.PACKAGE,
                maschinePriority);
        this.recipe = recipe;
    }

    @Override
    protected void checkStorageStatus() {
        try {
            Status newStatus = StatusInfo.OPERATIONAL;
            storageSemaphore.acquire();
            logger.info("Checking storage status of PackagingMaschine " + identificationNumber);
            // Check recipe ingredients
            for (Cargo cargo : recipe.ingredients().keySet()) {
                int storedQuantity = storage.getOrDefault(cargo, 0);
                int ingredientQuantity = recipe.ingredients().get(cargo);
                if (storedQuantity == 0) {
                    if (newStatus != StatusWarning.EMPTY) {
                        newStatus = StatusWarning.EMPTY;
                        logger.info("Ingredient " + cargo + " is empty in PackagingMaschine " + identificationNumber);
                    }
                    if (cargo.getCargoTyp() == CargoTyp.MATERIAL){
                        sendCargoRequest(cargo, maxStorageCapacity);
                    }
                } else if (storedQuantity <= maxStorageCapacity * 0.25 || storedQuantity < ingredientQuantity) {
                    if (newStatus != StatusWarning.EMPTY) {
                        newStatus = StatusCritical.LOW_CAPACITY;
                        logger.info("Ingredient " + cargo + " is low in PackagingMaschine " + identificationNumber);
                    }
                    if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
                        sendCargoRequest(cargo, maxStorageCapacity - storedQuantity);
                    }
                }
                else if (storedQuantity >= maxStorageCapacity) {
                    if (newStatus != StatusWarning.EMPTY && newStatus != StatusCritical.LOW_CAPACITY) {
                        newStatus = StatusWarning.FULL;
                        logger.info("Ingredient " + cargo + " storage is FULL in PackagingMaschine " + identificationNumber);
                    }
                }
            }
            // Check product storage
            int productStorage = storage.getOrDefault(productCargo, 0);
            if (productStorage >= maxStorageCapacity) {
                if (newStatus != StatusWarning.FULL) {
                    newStatus = StatusWarning.FULL;
                    logger.info("Product storage is FULL in PackagingMaschine " + identificationNumber);
                    sendCargoRequest(productCargo, productStorage);
                }
            } else if (productStorage >= maxStorageCapacity * 0.75) {
                if (newStatus != StatusCritical.LOW_CAPACITY) {
                    newStatus = StatusCritical.LOW_CAPACITY;
                    logger.info("Product storage is LOW_CAPACITY in PackagingMaschine " + identificationNumber);
                    sendCargoRequest(productCargo, productStorage);
                }
            }
            status = newStatus;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected void checkIfCargoProductionIsPossible() {
        boolean cargoProductionIsPossible = true;
        try {
            storageSemaphore.acquire();
            logger.info("Checking if cargo production is possible");
            for (Cargo cargo : recipe.ingredients().keySet()) {
                int ingredientQuantity = recipe.ingredients().get(cargo);
                int storedQuantity = storage.getOrDefault(cargo, 0);
                if (storedQuantity < ingredientQuantity) {
                    cargoProductionIsPossible = false;
                    if (running) {
                        logger.info("Packaging Machine " + identificationNumber + " lacks ingredient " + cargo + " for production");
                    }
                }
            }
            int currentProductQuantity = storage.getOrDefault(productCargo, 0);
            if (currentProductQuantity >= maxStorageCapacity) {
                logger.info("Storage full, cannot produce more product of " + identificationNumber);
                cargoProductionIsPossible = false;
                if (running) {
                    logger.info("Packaging Machine " + identificationNumber + " storage full for product " + productCargo);
                }
            }
            if (!cargoProductionIsPossible && running) {
                stopMachine();
                System.out.println("Packaging Machine " + identificationNumber + " stopped due to insufficient ingredients or full storage");
            }
            if (cargoProductionIsPossible && !running) {
                startMachine();
                System.out.println("Packaging Machine " + identificationNumber + " started production");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected Cargo produceProduct() {
        try {
            logger.info("Produce product of " + identificationNumber);
            storageSemaphore.acquire();
            for (Cargo cargo : recipe.ingredients().keySet()) {
                int ingredientQuantity = recipe.ingredients().get(cargo);
                if (storage.containsKey(cargo)) {
                    int storedQuantity = storage.get(cargo);
                    storage.put(cargo, storedQuantity - ingredientQuantity);
                }
                else
                {
                    throw new IllegalArgumentException("Ingredient not found in storage");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return productCargo;
    }

    @Override
    protected void storeProductOrDeliverToNextMachine(Cargo cargo) {
        storeProduct(cargo);
    }
}
