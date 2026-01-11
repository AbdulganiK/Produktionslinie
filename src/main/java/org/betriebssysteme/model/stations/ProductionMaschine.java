package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;

public class ProductionMaschine extends Maschine {
    private Recipe recipe;

    public ProductionMaschine(int identificationNumber,
                              int timeToSleep,
                              int maxStorageCapacity,
                              ProductionHeadquarters productionHeadquarters,
                              Maschine nextMaschine,
                              Recipe recipe,
                              int initialQuantityOfProduct) {
        super(identificationNumber,
                recipe.productionTime(),
                timeToSleep,
                maxStorageCapacity,
                productionHeadquarters,
                nextMaschine,
                recipe.getInitalStorage(initialQuantityOfProduct),
                recipe.productCargo());
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
                        status = Status.EMPTY;
                        sendCargoRequest(cargo, maxStorageCapacity);
                        return;
                    } else if (storedQuantity <= ingredientQuantity * 0.25) {
                        status = Status.LOW_CAPACITY;
                        sendCargoRequest(cargo, maxStorageCapacity - storedQuantity);
                        return;
                    }
                    else{
                        requestedCargoTypes.put(cargo, false);
                    }
                } else {
                    if (storedQuantity >= maxStorageCapacity) {
                        status = Status.FULL;
                        sendCargoRequest(cargo, maxStorageCapacity);
                        return;
                    } else if (storedQuantity >= maxStorageCapacity * 0.75) {
                        status = Status.LOW_CAPACITY;
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
                if (storedQuantity < ingredientQuantity && running) {
                    logger.info("Not enough ingredients to produce product of " + identificationNumber);
                    cargoPrductionIsPossible = false;
                }
            }
            //int currentProductQuantity = storage.getOrDefault(productCargo, 0);
            //if (currentProductQuantity >= maxStorageCapacity && running) {
            //    logger.info("Storage full, cannot produce more product of " + identificationNumber);
            //    cargoPrductionIsPossible = false;
            //}
            // TODO decide if we want to stop production when storage is full
            if (!cargoPrductionIsPossible && running) {
                stopMachine();
            }
            if (cargoPrductionIsPossible && !running) {
                startMachine();
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
        return productCargo;
    }

    @Override
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        deliverToNextMachine(cargo);
    }
}
