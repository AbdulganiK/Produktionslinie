package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.ProductionGood;
import org.betriebssysteme.model.Storage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;


public abstract class Machine extends Thread {

    protected volatile boolean running = false;
    protected Storage storage;
    protected final BlockingQueue<ProductionGood> productionGoodQueue;
    protected int production_time_in_ms;
    protected int material_storage_capacity;
    protected Semaphore machine_semaphore = new Semaphore(1);

    protected Machine(int productionGoodStorageCapacity, Storage storage, int production_time_in_ms, int material_storage_capacity) {
        if (storage == null) {
            throw new RuntimeException("Storage cannot be null");
        }
        this.storage = storage;
        if (material_storage_capacity >= 1) {
            this.material_storage_capacity = material_storage_capacity;
        } else {
            throw new RuntimeException("Material storage capacity must be at least 1");
        }
        if (production_time_in_ms > 0) {
            this.production_time_in_ms = production_time_in_ms;
        } else {
            throw new RuntimeException("Production time must be greater than 0");
        }
        if (productionGoodStorageCapacity >= 1) {
            this.productionGoodQueue = new ArrayBlockingQueue<>(productionGoodStorageCapacity);
        } else {
            throw new RuntimeException("Production good storage capacity must be at least 1");
        }
    }

    @Override
    public void run() {
        while (true) {
            if (running) {
                produce();
                refill_emptying_needed();
                try {
                    wait(100); // Kurze Pause, um CPU-Überlastung zu vermeiden
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Methode dient zum Start der Maschine
     */
    public void startP() {
        running = true;
    }

    /**
     * Methode dient zum stoppen der Maschine
     */
    public void stopP() {
        running = false;
    }

    /**
     * Methode dient zum produzieren von gütern in der run methode
     */
    abstract void produce();

    abstract boolean canProduce();

    abstract void refill_emptying_needed();

    abstract ProductionGood produce_good();

    abstract int get_production_good(ProductionGood productionGood, int transfer_amount);

    abstract int refill_material(MaterialType materialType, int amount);
}
