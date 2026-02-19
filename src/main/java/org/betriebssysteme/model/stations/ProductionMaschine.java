package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;

/**
 * The ProductionMaschine class represents a machine responsible for producing products in the production line.
 * It extends the Maschine class and implements specific behavior for a production machine.
 */
public class ProductionMaschine extends Maschine {
    private final Recipe recipe;

    /**
     * Constructor for the ProductionMaschine class.
     * @param identificationNumber ID of the ProductionMaschine
     * @param timeToSleep time taken to sleep between production cycles in milliseconds
     * @param maxStorageCapacity maximum storage capacity for ingredients in the machine
     * @param nextMaschine the next machine to which the produced cargo will be delivered
     * @param recipe the recipe that defines the ingredients for the product, the production time, and the type of product cargo
     * @param initialQuantityOfProduct the initial quantity of the product cargo in storage
     * @param maschinePriority the priority of the machine for requests
     */
    public ProductionMaschine(int identificationNumber,
                              int timeToSleep,
                              int maxStorageCapacity,
                              Maschine nextMaschine,
                              Recipe recipe,
                              int initialQuantityOfProduct,
                              int maschinePriority) {
        super(identificationNumber,
                recipe.productionTime(),
                timeToSleep,
                maxStorageCapacity,
                nextMaschine,
                recipe.getInitialStorage(initialQuantityOfProduct),
                recipe.productCargo(),
                maschinePriority);
        this.recipe = recipe;
    }

    @Override
    protected void checkStorageStatus() {
        try {
            Status newStatus = StatusInfo.OPERATIONAL;
            logger.info("Checking storage status of ProductionMaschine {}", identificationNumber);
            storageSemaphore.acquire();
            for (Cargo cargo : recipe.ingredients().keySet()) {
                int storedQuantity = storage.getOrDefault(cargo, 0);
                int ingredientQuantity = recipe.ingredients().get(cargo);
                if (storedQuantity == 0) {
                    newStatus = StatusWarning.EMPTY;
                    logger.info("Ingredient {} is empty in ProductionMaschine {}", cargo, identificationNumber);
                    if (cargo.getCargoTyp() == CargoTyp.MATERIAL){
                        sendCargoRequest(cargo, maxStorageCapacity);
                    }
                } else if (storedQuantity <= maxStorageCapacity * 0.25 || storedQuantity < ingredientQuantity) {
                    if (newStatus != StatusWarning.EMPTY) {
                        newStatus = StatusCritical.LOW_CAPACITY;
                        logger.info("Ingredient {} is low in ProductionMaschine {}", cargo, identificationNumber);
                    }
                    if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
                        sendCargoRequest(cargo, maxStorageCapacity - storedQuantity);
                    }
                }
                else if (storedQuantity >= maxStorageCapacity) {
                    if (newStatus != StatusWarning.EMPTY && newStatus != StatusCritical.LOW_CAPACITY) {
                        newStatus = StatusWarning.FULL;
                        logger.info("Ingredient {} storage is FULL in ProductionMaschine {}", cargo, identificationNumber);
                    }
                }
                status = newStatus;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
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
                    break;
                }
            }
            if (!cargoProductionIsPossible && running) {
                stopMachine();
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("ProductionMaschine " + identificationNumber + " stopped due to insufficient ingredients");
                }
            }
            if (cargoProductionIsPossible && !running) {
                startMachine();
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("ProductionMaschine " + identificationNumber + " started as all ingredients are available");
                }
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
        deliverToNextMachine(cargo);
    }
}
