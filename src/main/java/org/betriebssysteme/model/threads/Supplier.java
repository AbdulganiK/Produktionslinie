package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.Storage;

public class Supplier extends Thread {
    private Storage storage;
    private int kapacity;
    private int supplyIntervalMillis;

    public Supplier(Storage storage, int kapacity, int supplyIntervalMillis) {
        this.storage = storage;
        this.kapacity = kapacity;
        this.supplyIntervalMillis = supplyIntervalMillis;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int amountSupplied = supplyMaterials();
                take_srcap_packets(amountSupplied);
                Thread.sleep(supplyIntervalMillis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int supplyMaterials() {
        MaterialType materialType = MaterialType.values()[(int) (Math.random() * MaterialType.values().length)];
        int suppliedAmount = 0;
        if (storage.check_storage_available(materialType)){
            suppliedAmount = storage.refill_storage(materialType, kapacity);
            System.out.println("Supplier supplied " + suppliedAmount + " units of " + materialType);
            storage.release_storage(materialType);
        }
        else {
            System.out.println("Supplier found no available storage for " + materialType);
        }
        if (suppliedAmount < kapacity) {
            System.out.println("Supplier could only supply " + suppliedAmount + " out of " + kapacity + " units of " + materialType + " due to storage limits.");
        }
        return suppliedAmount;
    }

    private void take_srcap_packets(int suppliedAmount) {
        int kapacity_for_scrap_packets = kapacity - suppliedAmount;
        int amount_of_packets = 0;
        if (kapacity_for_scrap_packets > 0) {
            if (storage.check_storage_available(MaterialType.PACKETS)) {
                amount_of_packets = storage.get_packets(kapacity_for_scrap_packets);
                System.out.println("Supplier checking for packets to utilize remaining capacity of " + amount_of_packets);
                storage.release_storage(MaterialType.PACKETS);
            } else {
                System.out.println("Supplier found no available storage for PACKETS");
            }
            int kapacity_left = kapacity_for_scrap_packets - amount_of_packets;
            if (kapacity_left > 0) {
                if (storage.check_storage_available(MaterialType.SCRAP)) {
                    int amount_of_scrap = storage.get_scrap(kapacity_left);
                    System.out.println("Supplier checking for scrap to utilize remaining capacity of " + amount_of_scrap);
                    storage.release_storage(MaterialType.SCRAP);
                } else {
                    System.out.println("Supplier found no available storage for SCRAP");
                }

            }
        }
    }
}
