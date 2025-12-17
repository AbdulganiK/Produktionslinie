package org.betriebssysteme.model;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Storage {
    private final int storage_kapazitaet = 10000; //in weight and size units

    private int storage_plastic;
    private Semaphore storage_plastic_semaphore = new Semaphore(1);

    private int storage_rubber;
    private Semaphore storage_rubber_semaphore = new Semaphore(1);

    private int storage_smd; //smd components likes resistors, capacitors, transistors, chips
    private Semaphore storage_smd_semaphore = new Semaphore(1);

    private int storage_displays;
    private Semaphore storage_displays_semaphore = new Semaphore(1);

    private int storage_motors;
    private Semaphore storage_motors_semaphore = new Semaphore(1);

    private int storage_pcbs;
    private Semaphore storage_pcbs_semaphore = new Semaphore(1);

    private int storage_sensors;
    private Semaphore storage_sensors_semaphore = new Semaphore(1);

    private int storage_lithium_batteries;
    private Semaphore storage_lithium_batteries_semaphore = new Semaphore(1);

    private int storage_packets;
    private Semaphore storage_packets_semaphore = new Semaphore(1);

    private int storage_scrap;
    private Semaphore storage_scrap_semaphore = new Semaphore(1);

    private int storage_glue;
    private Semaphore storage_glue_semaphore = new Semaphore(1);

    private int storage_packing_material;
    private Semaphore storage_packing_material_semaphore = new Semaphore(1);

    private Queue<Storage_request> requests = new PriorityQueue<>((request1, request2) -> Integer.compare(request2.priority(), request1.priority()));

    public Storage_request get_warehouse_operator_task() {
        if (requests.isEmpty()) {
            return null;
        }
        return requests.poll();
    }

    public int refill_storage(MaterialType material, int amount) {
        switch (material) {
            case PLASTIC:
                if (storage_plastic + amount <= storage_kapazitaet) {
                    storage_plastic += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_plastic;
                    storage_plastic += new_amount;
                    return amount - new_amount;
                }
            case RUBBER:
                if (storage_rubber + amount <= storage_kapazitaet) {
                    storage_rubber += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_rubber;
                    storage_rubber += new_amount;
                    return amount - new_amount;
                }
            case SMD_COMPONENTS:
                if (storage_smd + amount <= storage_kapazitaet) {
                    storage_smd += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_smd;
                    storage_smd += new_amount;
                    return amount - new_amount;
                }
            case DISPLAYS:
                if (storage_displays + amount <= storage_kapazitaet) {
                    storage_displays += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_displays;
                    storage_displays += new_amount;
                    return amount - new_amount;
                }
            case MOTORS:
                if (storage_motors + amount <= storage_kapazitaet) {
                    storage_motors += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_motors;
                    storage_motors += new_amount;
                    return amount - new_amount;
                }
            case PCBS:
                if (storage_pcbs + amount <= storage_kapazitaet) {
                    storage_pcbs += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_pcbs;
                    storage_pcbs += new_amount;
                    return amount - new_amount;
                }
            case SENSORS:
                if (storage_sensors + amount <= storage_kapazitaet) {
                    storage_sensors += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_sensors;
                    storage_sensors += new_amount;
                    return amount - new_amount;
                }
            case LITHIUM_BATTERIES:
                if (storage_lithium_batteries + amount <= storage_kapazitaet) {
                    storage_lithium_batteries += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_lithium_batteries;
                    storage_lithium_batteries += new_amount;
                    return amount - new_amount;
                }
            case GLUE:
                if (storage_glue + amount <= storage_kapazitaet) {
                    storage_glue += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_glue;
                    storage_glue += new_amount;
                    return amount - new_amount;
                }
            case PACKING_MATERIAL:
                if (storage_packing_material + amount <= storage_kapazitaet) {
                    storage_packing_material += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_packing_material;
                    storage_packing_material += new_amount;
                    return amount - new_amount;
                }
            case PACKETS:
                if (storage_packets + amount <= storage_kapazitaet) {
                    storage_packets += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_packets;
                    storage_packets += new_amount;
                    return amount - new_amount;
                }
            case SCRAP:
                if (storage_scrap + amount <= storage_kapazitaet) {
                    storage_scrap += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_scrap;
                    storage_scrap += new_amount;
                    return amount - new_amount;
                }
            default:
                return amount;
        }
    }

    public int get_storage_kapazitaet() {
        return storage_kapazitaet;
    }

    public int get_storage_plastic(int amount) {
        if (storage_plastic < amount) {
            amount = storage_plastic;
        }
        storage_plastic -= amount;
        return amount;
    }

    public int get_storage_rubber(int amount) {
        if (storage_rubber < amount) {
            amount = storage_rubber;
        }
        storage_rubber -= amount;
        return amount;
    }

    public int get_storage_smd(int amount) {
        if (storage_smd < amount) {
            amount = storage_smd;
        }
        storage_smd -= amount;
        return amount;
    }

    public int get_storage_displays(int amount) {
        if (storage_displays < amount) {
            amount = storage_displays;
        }
        storage_displays -= amount;
        return amount;
    }

    public int get_storage_motors(int amount) {
        if (storage_motors < amount) {
            amount = storage_motors;
        }
        storage_motors -= amount;
        return amount;
    }

    public int get_storage_pcbs(int amount) {
        if (storage_pcbs < amount) {
            amount = storage_pcbs;
        }
        storage_pcbs -= amount;
        return amount;
    }

    public int get_storage_sensors(int amount) {
        if (storage_sensors < amount) {
            amount = storage_sensors;
        }
        storage_sensors -= amount;
        return amount;
    }

    public int get_storage_lithium_batteries(int amount) {
        if (storage_lithium_batteries < amount) {
            amount = storage_lithium_batteries;
        }
        storage_lithium_batteries -= amount;
        return amount;
    }

    public int get_packets(int amount) {
        if (storage_packets < amount) {
            amount = storage_packets;
        }
        storage_packets -= amount;
        return amount;
    }

    public int get_scrap(int amount) {
        if (storage_scrap < amount) {
            amount = storage_scrap;
        }
        storage_scrap -= amount;
        return amount;
    }

    public int get_glue(int amount) {
        if (storage_glue < amount) {
            amount = storage_glue;
        }
        storage_glue -= amount;
        return amount;
    }

    public int get_packing_material(int amount) {
        if (storage_packing_material < amount) {
            amount = storage_packing_material;
        }
        storage_packing_material -= amount;
        return amount;
    }

    public void add_request(Storage_request request) {
        requests.add(request);
    }

    public boolean check_storage_available(MaterialType type) {
        switch (type) {
            case PLASTIC:
                if (storage_plastic_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case RUBBER:
                if (storage_rubber_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case SMD_COMPONENTS:
                if (storage_smd_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case DISPLAYS:
                if (storage_displays_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case MOTORS:
                if (storage_motors_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case PCBS:
                if (storage_pcbs_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case SENSORS:
                if (storage_sensors_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case LITHIUM_BATTERIES:
                if (storage_lithium_batteries_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case PACKETS:
                if (storage_packets_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case SCRAP:
                if (storage_scrap_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case GLUE:
                if (storage_glue_semaphore.tryAcquire()) {
                return true;
                }
                else
                    return false;
            case PACKING_MATERIAL:
                if (storage_packing_material_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            default:
                return false;
        }
    }

    public void release_storage(MaterialType type) {
        switch (type) {
            case PLASTIC:
                storage_plastic_semaphore.release();
                break;
            case RUBBER:
                storage_rubber_semaphore.release();
                break;
            case SMD_COMPONENTS:
                storage_smd_semaphore.release();
                break;
            case DISPLAYS:
                storage_displays_semaphore.release();
                break;
            case MOTORS:
                storage_motors_semaphore.release();
                break;
            case PCBS:
                storage_pcbs_semaphore.release();
                break;
            case SENSORS:
                storage_sensors_semaphore.release();
                break;
            case LITHIUM_BATTERIES:
                storage_lithium_batteries_semaphore.release();
                break;
            case PACKETS:
                storage_packets_semaphore.release();
                break;
            case SCRAP:
                storage_scrap_semaphore.release();
                break;
            case GLUE:
                storage_glue_semaphore.release();
                break;
            case PACKING_MATERIAL:
                storage_packing_material_semaphore.release();
                break;
            default:
                break;
        }
    }
}
