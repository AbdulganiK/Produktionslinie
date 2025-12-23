package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.ProductionGood;
import org.betriebssysteme.model.Storage;
import org.betriebssysteme.model.Storage_request;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;


public abstract class Machine extends Thread {

    protected volatile boolean running = false;
    protected Storage storage;
    protected final Map<MaterialType, ArrayBlockingQueue<MaterialType>> material_storage;
    protected Map<MaterialType, Boolean> requested_materials = new HashMap<>();
    protected final BlockingQueue<ProductionGood> productionGoodQueue;
    protected boolean requested_emptying_productionGoodQueue = false;
    protected final BlockingQueue<ProductionGood> holding_area;
    protected boolean requested_emptying_holding_area = false;
    protected int holding_area_capacity;
    protected int productionGoodStorageCapacity;
    protected int material_storage_capacity;
    protected int production_time_in_ms;
    protected Semaphore machine_semaphore = new Semaphore(1);
    protected int material_priority_level;
    protected int waiting_time_run_ms;

    public Machine(final Storage storage,
                   final int production_time_in_ms,
                   final int material_storage_capacity,
                   MaterialType[] materialTypes,
                   final int waiting_time_run_ms,
                   final int material_priority_level,
                   final int holding_area_capacity,
                   final int productionGoodStorageCapacity

    ) {

        // create all material storages
        if (material_storage_capacity > 0) {
            this.material_storage_capacity = material_storage_capacity;
        } else {
            throw new RuntimeException("Material storage capacity must be at least 1");
        }
        this.material_storage = new HashMap<MaterialType, ArrayBlockingQueue<MaterialType>>();
        for (MaterialType materialType : materialTypes) {
            material_storage.put(materialType, new ArrayBlockingQueue<MaterialType>(material_storage_capacity));
            requested_materials.put(materialType, false);
        }

        // write other parameters
        if (storage == null) {
            throw new RuntimeException("Storage cannot be null");
        }
        this.storage = storage;
        if (production_time_in_ms > 0) {
            this.production_time_in_ms = production_time_in_ms;
        } else {
            throw new RuntimeException("Production time must be greater than 0");
        }
        if (productionGoodStorageCapacity >= 0) {
            this.productionGoodQueue = new ArrayBlockingQueue<>(productionGoodStorageCapacity);
            this.productionGoodStorageCapacity = productionGoodStorageCapacity;
        } else {
            throw new RuntimeException("Production good storage capacity must be at least 0");
        }
        if (material_priority_level >= 1 && material_priority_level <=5) {
            this.material_priority_level = material_priority_level;
        } else {
            throw new RuntimeException("Material priority level must be between 1 and 5");
        }
        if (waiting_time_run_ms > 0) {
            this.waiting_time_run_ms = waiting_time_run_ms;
        } else {
            throw new RuntimeException("Waiting time in run method must be at least 1 ms");
        }
        if (holding_area_capacity >= 0) {
            this.holding_area = new ArrayBlockingQueue<>(holding_area_capacity);
            this.holding_area_capacity = holding_area_capacity;
        } else {
            throw new RuntimeException("Holding area capacity must be at least 0");
        }

    }


    public void startMachine() {
        running = true;
    }


    public void stopMachine() {
        running = false;
    }

    public boolean reservertingMachine(){
        return machine_semaphore.tryAcquire();
    }

    public void releaseMachine(){
        machine_semaphore.release();
    }


    private void check_material_storage_levels(){
        for (MaterialType materialType : material_storage.keySet()) {
            ArrayBlockingQueue<MaterialType> storageQueue = material_storage.get(materialType);
            if (storageQueue.isEmpty()) {
                stopMachine();
            }
        }

        if (productionGoodQueue.remainingCapacity() == 0 && holding_area_capacity > 0) {
            stopMachine();
        }

        if (holding_area.remainingCapacity() == 0 && holding_area_capacity > 0) {
            stopMachine();
        }
    }


    abstract void dispatch_or_store_produced_goods(ProductionGood productionGood);

    abstract ProductionGood produce_good();


    private void run_produce_cycle(){
        ProductionGood productionGood = produce_good();
        dispatch_or_store_produced_goods(productionGood);
    }


    private void send_material_refill_requests(){
        for (MaterialType materialType : material_storage.keySet()) {
            ArrayBlockingQueue<MaterialType> storageQueue = material_storage.get(materialType);
            if (storageQueue.size() < 0.25 * material_storage_capacity && requested_materials.get(materialType)) {
                int amountNeeded = material_storage_capacity - storageQueue.size();
                Storage_request request = new Storage_request(materialType, amountNeeded, 2);
                storage.add_request(request);
            }
        }
    }


    private void send_holding_area_emptying_request(){
        if (holding_area_capacity > 0) {
            if (holding_area.remainingCapacity() < 0.25 * holding_area_capacity && !requested_emptying_productionGoodQueue) {
                Storage_request request = new Storage_request(MaterialType.REQUETED_EMPTYING, holding_area_capacity, 1);
                storage.add_request(request);
            }
        }
    }


    private void send_production_good_emptying_request(){
        if (productionGoodStorageCapacity > 0) {
            if (productionGoodQueue.remainingCapacity() < 0.25 * productionGoodStorageCapacity && !requested_emptying_holding_area) {
                Storage_request request = new Storage_request(MaterialType.REQUETED_EMPTYING, productionGoodStorageCapacity, 1);
                storage.add_request(request);
            }
        }
    }


    private void check_if_refill_or_emptying_needed(){
        send_material_refill_requests();
        send_holding_area_emptying_request();
        send_production_good_emptying_request();
    }


    @Override
    public void run() {
        while (true) {
            reservertingMachine(); // Maschine reservieren
            check_material_storage_levels(); // Prüfen, ob Materialen vorliegen oder ob Platz für produzierte Güter vorhanden ist
            if (running) {
                run_produce_cycle();
            }
            check_if_refill_or_emptying_needed();
            releaseMachine();
            try {
                wait(waiting_time_run_ms); // Kurze Pause, um CPU-Überlastung zu vermeiden
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int refill_material(MaterialType materialType, int amount) {
        int filled = 0;
        if (reservertingMachine()) {
            if (!requested_materials.get(materialType)) {
                releaseMachine();
                return filled;
            }
            else {
                ArrayBlockingQueue<MaterialType> storageQueue = material_storage.get(materialType);
                if (storageQueue != null) {
                    while (filled < amount && storageQueue.size() < material_storage_capacity) {
                        storageQueue.add(materialType);
                        filled++;
                    }
                    requested_materials.put(materialType, false);
                }
            }
            releaseMachine();
        }
        return filled;
    }

    public int get_production_good(ProductionGood productionGood, int transfer_amount) {
        int transferred = 0;
        if (reservertingMachine()){
            while (transferred < transfer_amount && !productionGoodQueue.isEmpty()) {
                productionGoodQueue.poll();
                transferred++;
            }
            startMachine();
            requested_emptying_holding_area = false;
        }
        releaseMachine();
        return transferred;
    }

    public int get_holding_area_goods(ProductionGood productionGood, int transfer_amount) {
        int transferred = 0;
        if (reservertingMachine()){
            while (transferred < transfer_amount && !holding_area.isEmpty()) {
                holding_area.poll();
                transferred++;
            }
            startMachine();
            requested_emptying_productionGoodQueue = false;
        }
        releaseMachine();
        return transferred;
    }
}
