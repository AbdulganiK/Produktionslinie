package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.ProductionGood;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


public abstract class Machine extends Thread {

    protected volatile boolean running = false;
    protected final BlockingQueue<MaterialType> materialQueue;
    protected final BlockingQueue<ProductionGood> productionGoodQueue;


    protected Machine(int loadingCapacity, int storageCapacity) {
        if (loadingCapacity > 0) {
            this.materialQueue = new ArrayBlockingQueue<>(loadingCapacity);
            this.productionGoodQueue = new ArrayBlockingQueue<>(storageCapacity);
        } else {
            throw new RuntimeException("Ladekapazität zu gering!");
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

    /**
     * Methode dient zum beladen der Maschine mit materialien
     * @param material
     */
    public void load(MaterialType material){
        materialQueue.add(material);
    }

    /**
     * Methode dient zum entladen der Produktionsgüter aus dem Zwischenlager
     * @return
     * @throws InterruptedException
     */
    public ProductionGood unload() throws InterruptedException {
        return productionGoodQueue.take();
    }




}
