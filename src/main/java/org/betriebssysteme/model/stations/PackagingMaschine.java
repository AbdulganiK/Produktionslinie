package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Recipe;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.Material;
import org.betriebssysteme.model.cargo.Product;
import org.betriebssysteme.model.status.StatusCritical;
import org.betriebssysteme.model.status.StatusWarning;

public class PackagingMaschine extends Maschine {
    private Recipe recipe;
    public PackagingMaschine(int identificationNumber,
                              int timeToSleep,
                              int maxStorageCapacity,
                              Maschine nextMaschine,
                              int initialQuantityOfProduct,
                              int productionTime,
                              Recipe recipe,
                             int maschinePriority
                             ) {
        super(identificationNumber,
                productionTime,
                timeToSleep,
                maxStorageCapacity,
                nextMaschine,
                recipe.getInitalStorageWithProduct(initialQuantityOfProduct),
                Product.SHIPPING_PACKAGE,
                maschinePriority);
        this.recipe = recipe;
    }

    @Override
    protected void checkStorageStatus() {
        try {
            logger.info("Checking storage status of ProductionMaschine " + identificationNumber);
            storageSemaphore.acquire();
            int productStorage = storage.getOrDefault(Product.SHIPPING_PACKAGE, 0);
            if (productStorage >= maxStorageCapacity) {
                if(status != StatusWarning.FULL){
                    status = StatusWarning.FULL;
                    logger.info("Product storage is FULL in PackagingMaschine " + identificationNumber);
                }
                sendCargoRequest(productCargo, maxStorageCapacity);
                return;
            } else if (productStorage >= maxStorageCapacity * 0.75) {
                if(status != StatusCritical.LOW_CAPACITY && status != StatusWarning.FULL){
                    status = StatusCritical.LOW_CAPACITY;
                    logger.info("Product storage is LOW_CAPACITY in PackagingMaschine " + identificationNumber);
                }
                sendCargoRequest(productCargo, productStorage);
                return;
            }
            int storagePackingMaterial = storage.getOrDefault(Material.PACKING_MATERIAL, 0);
            if (storagePackingMaterial == 0) {
                if (status != StatusWarning.EMPTY) {
                    status = StatusWarning.EMPTY;
                    logger.info("Packing material is EMPTY in PackagingMaschine " + identificationNumber);
                }
                sendCargoRequest(Material.PACKING_MATERIAL, maxStorageCapacity);
                return;
            } else if (storagePackingMaterial <= maxStorageCapacity * 0.25) {
                if (status != StatusCritical.LOW_CAPACITY && status != StatusWarning.EMPTY) {
                    status = StatusCritical.LOW_CAPACITY;
                    logger.info("Packing material is LOW_CAPACITY in PackagingMaschine " + identificationNumber);
                }
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
                if (storedQuantity < ingredientQuantity) {
                    cargoPrductionIsPossible = false;
                    if (running) {
                        System.out.println("Packaging Machine " + identificationNumber + " lacks ingredient " + cargo + " for production");
                    }
                }
            }
            int currentProductQuantity = storage.getOrDefault(productCargo, 0);
            if (currentProductQuantity >= maxStorageCapacity) {
                logger.info("Storage full, cannot produce more product of " + identificationNumber);
                cargoPrductionIsPossible = false;
                if (running) {
                    System.out.println("Packaging Machine " + identificationNumber + " storage full for product " + productCargo);
                }
            }
            if (!cargoPrductionIsPossible && running) {
                stopMachine();
                System.out.println("Packaging Machine " + identificationNumber + " stopped due to insufficient ingredients or full storage");
            }
            if (cargoPrductionIsPossible && !running) {
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
    protected void storePrductOrDeliverToNextMachine(Cargo cargo) {
        storeProduct(cargo);
    }
}
