package org.betriebssysteme.model.threads;

import org.betriebssysteme.model.MaterialType;
import org.betriebssysteme.model.ProductionGood;
import org.betriebssysteme.model.Storage;
import org.betriebssysteme.model.Storage_request;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Produktionmachine_board_drive_unit extends Machine{
    private Queue<MaterialType> machine_storage_circuit_board_components;
    private Queue<MaterialType> machine_storage_motors;

    protected Produktionmachine_board_drive_unit(int material_storage_capacity, int production_good_storage_capacity, Storage storage, int production_time_in_ms) {
        super(production_good_storage_capacity, storage, production_time_in_ms, material_storage_capacity);
        machine_storage_circuit_board_components = new ArrayBlockingQueue<>(material_storage_capacity);
        machine_storage_motors = new ArrayBlockingQueue<>(material_storage_capacity);
    }

    @Override
    void produce() {
        if (canProduce()) {
            try {
                Thread.sleep(production_time_in_ms);
                productionGoodQueue.put(produce_good());
                // Verbrauchte Materialien entfernen
                machine_storage_circuit_board_components.poll();
                machine_storage_motors.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    boolean canProduce() {
        if (machine_storage_circuit_board_components.isEmpty() || machine_storage_motors.isEmpty() || productionGoodQueue.remainingCapacity() == 0) {
            stopP();
            return false;
        } else if (machine_semaphore.tryAcquire()) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    void refill_emptying_needed() {
        if (machine_storage_circuit_board_components.size() < 0.25 * material_storage_capacity) {
            Storage_request request = new Storage_request(MaterialType.CIRCUIT_BOARD_COMPONETS, material_storage_capacity - machine_storage_circuit_board_components.size(), 2);
            storage.add_request(request);
        }
        if (machine_storage_motors.size() < 0.25 * material_storage_capacity) {
            Storage_request request = new Storage_request(MaterialType.MOTORS, material_storage_capacity - machine_storage_motors.size(), 2);
            storage.add_request(request);
        }
        if (productionGoodQueue.remainingCapacity() < 0.25 * productionGoodQueue.size()) {
            Storage_request request = new Storage_request(MaterialType.REQUETED_EMPTYING, productionGoodQueue.size(), 1);
            storage.add_request(request);
        }
    }

    @Override
    ProductionGood produce_good(){
        try {
            wait(production_time_in_ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        machine_storage_circuit_board_components.poll();
        machine_storage_motors.poll();
        machine_semaphore.release();
        return ProductionGood.DRIVE_PCB;
    }

    @Override
    int get_production_good(ProductionGood productionGood, int transfer_amount) {
        int transferred = 0;
        while (transferred < transfer_amount && !productionGoodQueue.isEmpty()) {
            productionGoodQueue.poll();
            transferred++;
        }
        return transferred;
    }

    @Override
    int refill_material(MaterialType materialType, int amount) {
        int filled = 0;
        switch (materialType) {
            case CIRCUIT_BOARD_COMPONETS:
                while (filled < amount && machine_storage_circuit_board_components.size() < material_storage_capacity) {
                    machine_storage_circuit_board_components.add(MaterialType.CIRCUIT_BOARD_COMPONETS);
                    filled++;
                }
                break;
            case MOTORS:
                while (filled < amount && machine_storage_motors.size() < material_storage_capacity) {
                    machine_storage_motors.add(MaterialType.MOTORS);
                    filled++;
                }
                break;
            default:
                break;
        }
        if (filled > 0) {
            startP();
        }
        return filled;
    }
}
