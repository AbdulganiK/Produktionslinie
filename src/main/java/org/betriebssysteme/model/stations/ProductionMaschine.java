package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusWarning;

public class ProductionMaschine extends Maschine {
    private Recipe recipe;

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
                recipe.getInitalStorage(initialQuantityOfProduct),
                recipe.productCargo(),
                maschinePriority);
        this.recipe = recipe;
    }

    @Override
    protected void checkStorageStatus() {
        try {
            logger.info("Checking storage status of ProductionMaschine " + identificationNumber);
            storageSemaphore.acquire();
            for (Cargo cargo : storage.keySet()) {
                logger.info("Checking cargo: " + cargo);
                int storedQuantity = storage.get(cargo);
                if (recipe.ingredients().containsKey(cargo)) {
                    int ingredientQuantity = recipe.ingredients().get(cargo);
                    if (storedQuantity == 0) {
                        if (status != StatusWarning.EMPTY) {
                            status = StatusWarning.EMPTY;
                            logger.info("Ingredient " + cargo + " is empty in ProductionMaschine " + identificationNumber);
                        }
                        sendCargoRequest(cargo, maxStorageCapacity);
                        return;
                    } else if (storedQuantity <= ingredientQuantity * 0.25) {
                        if (status != StatusCritical.LOW_CAPACITY && status != StatusWarning.EMPTY) {
                            status = StatusCritical.LOW_CAPACITY;
                            logger.info("Ingredient " + cargo + " is low in ProductionMaschine " + identificationNumber);
                        }
                        sendCargoRequest(cargo, maxStorageCapacity - storedQuantity);
                        return;
                    }
                } else {
                    if (storedQuantity >= maxStorageCapacity) {
                        if (status != StatusWarning.FULL) {
                            status = StatusWarning.FULL;
                            logger.info("Product " + cargo + " storage is full in ProductionMaschine " + identificationNumber);
                        }
                        sendCargoRequest(cargo, maxStorageCapacity);
                        return;
                    } else if (storedQuantity >= maxStorageCapacity * 0.75) {
                        if (status != StatusCritical.LOW_CAPACITY && status != StatusWarning.FULL) {
                            status = StatusCritical.LOW_CAPACITY;
                            logger.info("Product " + cargo + " storage is low in ProductionMaschine " + identificationNumber);
                        }
                        sendCargoRequest(cargo, storedQuantity);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            storageSemaphore.release();
        }
    }

    @Override
    protected void checkIfCargoPrductionIsPossible() {
        boolean cargoPrductionIsPossible = true;
        try {
            storageSemaphore.acquire();
            logger.info("Checking if cargo prduction is possible");
            for (Cargo cargo : recipe.ingredients().keySet()) {
                int ingredientQuantity = recipe.ingredients().get(cargo);
                int storedQuantity = storage.getOrDefault(cargo, 0);
                if (storedQuantity < ingredientQuantity) {
                    cargoPrductionIsPossible = false;
                    break;
                }
            }
            if (!cargoPrductionIsPossible && running) {
                stopMachine();
                System.out.println("ProductionMaschine " + identificationNumber + " stopped due to insufficient ingredients");
            }
            if (cargoPrductionIsPossible && !running) {
                startMachine();
                System.out.println("ProductionMaschine " + identificationNumber + " started as all ingredients are available");
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
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        deliverToNextMachine(cargo);
    }
}
