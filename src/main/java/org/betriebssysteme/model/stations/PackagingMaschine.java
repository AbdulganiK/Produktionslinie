package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;

public class PackagingMaschine extends Maschine {
    private Recipe recipe;
    public PackagingMaschine(int identificationNumber,
                              int timeToSleep,
                              int maxStorageCapacity,
                              ProductionHeadquarters productionHeadquarters,
                              Maschine nextMaschine,
                              int initialQuantityOfProduct,
                              int productionTime,
                              Recipe recipe
                             ) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                productionHeadquarters,
                nextMaschine,
                recipe.getInitalStorageWithProduct(initialQuantityOfProduct),
                Product.SHIPPING_PACKAGE);
        this.recipe = recipe;
    }

    @Override
    protected void checkStorageStatus() {
        try {
            logger.info("Checking storage status of ProductionMaschine " + identificationNumber);
            storageSemaphore.acquire();
            int productStorage = storage.getOrDefault(Product.SHIPPING_PACKAGE, 0);
            if (productStorage >= maxStorageCapacity) {
                status = Status.FULL;
                sendCargoRequest(productCargo, maxStorageCapacity);
                return;
            } else if (productStorage >= maxStorageCapacity * 0.75) {
                status = Status.LOW_CAPACITY;
                sendCargoRequest(productCargo, productStorage);
                return;
            }
            int storagePackingMaterial = storage.getOrDefault(Material.PACKING_MATERIAL, 0);
            if (storagePackingMaterial == 0) {
                status = Status.EMPTY;
                sendCargoRequest(Material.PACKING_MATERIAL, maxStorageCapacity);
                return;
            } else if (storagePackingMaterial <= maxStorageCapacity * 0.25) {
                status = Status.LOW_CAPACITY;
                sendCargoRequest(Material.PACKING_MATERIAL, maxStorageCapacity - storagePackingMaterial);
                return;
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
                    System.out.println("Packaging Machine " + identificationNumber + " does not have enough " + cargo + " to produce product.");
                    logger.info("Packaging Machine " + identificationNumber + " does not have enough " + cargo + " to produce product.");
                    cargoPrductionIsPossible = false;
                }
            }
            int currentProductQuantity = storage.getOrDefault(productCargo, 0);
            if (currentProductQuantity >= maxStorageCapacity && running) {
                logger.info("Storage full, cannot produce more product of " + identificationNumber);
                cargoPrductionIsPossible = false;
                if (running) {
                    System.out.println("Packaging Machine " + identificationNumber + " storage full for product " + productCargo);
                }
            }
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
        try {
            Thread.sleep(timeToProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return productCargo;
    }

    @Override
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        storeProduct(cargo);
    }
}
