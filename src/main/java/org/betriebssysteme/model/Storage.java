package org.betriebssysteme.model;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * This class represents the storage of the factory.
 * It contains methods to access and modify the storage.
 */
public class Storage {

    // ===== Attributes =====

    /**
     * Storage capacity for each material type.
     */
    private final int storage_kapazitaet;

    /**
     * Current fill level of the plastics storage.
     */
    private int storage_plastic;
    /**
     * Semaphore to control access to the plastics storage.
     */
    private Semaphore storage_plastic_semaphore = new Semaphore(1);


    /**
     * Current fill level of the circuit board components storage.
     */
    private int storage_circuit_board_components;
    /**
     * Semaphore to control access to the circuit board components storage.
     */
    private Semaphore storage_circuit_board_components_semaphore = new Semaphore(1);


    /**
     * Current fill level of the displays storage.
     */
    private int storage_displays;
    /**
     * Semaphore to control access to the displays storage.
     */
    private Semaphore storage_displays_semaphore = new Semaphore(1);


    /**
     * Current fill level of the motors storage.
     */
    private int storage_motors;
    /**
     * Semaphore to control access to the motors storage.
     */
    private Semaphore storage_motors_semaphore = new Semaphore(1);


    /**
     * Current fill level of the packets storage.
     */
    private int storage_packets;
    /**
     * Semaphore to control access to the packets storage.
     */
    private Semaphore storage_packets_semaphore = new Semaphore(1);

    /**
     * Current fill level of the scrap storage.
     */
    private int storage_scrap;
    /**
     * Semaphore to control access to the scrap storage.
     */
    private Semaphore storage_scrap_semaphore = new Semaphore(1);


    /**
     * Current fill level of the glue storage.
     */
    private int storage_glue;
    /**
     * Semaphore to control access to the glue storage.
     */
    private Semaphore storage_glue_semaphore = new Semaphore(1);


    /**
     * Current fill level of the packing material storage.
     */
    private int storage_packing_material;
    /**
     * Semaphore to control access to the packing material storage.
     */
    private Semaphore storage_packing_material_semaphore = new Semaphore(1);


    /**
     * Queue of storage requests for the warehouse operator.
     * With priority based on the request priority.
     */
    private Queue<Storage_request> requests = new PriorityQueue<>((request1, request2) -> Integer.compare(request2.priority(), request1.priority()));

    // ===== Constructor =====

    /**
     * Constructor for the Storage class.
     * @param storage_kapazitaet int capacity for each material type
     * @param initial_fill_level int initial fill level for each material type
     */
    public Storage(int storage_kapazitaet, int initial_fill_level) {
        this.storage_kapazitaet = storage_kapazitaet;
        this.storage_plastic = initial_fill_level;
        this.storage_circuit_board_components = initial_fill_level;
        this.storage_displays = initial_fill_level;
        this.storage_motors = initial_fill_level;
        this.storage_glue = initial_fill_level;
        this.storage_packing_material = initial_fill_level;
        this.storage_packets = initial_fill_level;
        this.storage_scrap = initial_fill_level;
    }

    // ===== methods for getting max storage capacity =====

    /**
     * Method to get the maximum storage capacity.
     * @return int maximum storage capacity
     */
    public int get_storage_kapazitaet() {
        return storage_kapazitaet;
    }

    // ===== methods vor storage requests =====

    /**
     * Method to get the next storage request for the warehouse operator.
     * @return Storage_request next request or null if no requests are available
     */
    public Storage_request get_warehouse_operator_task() {
        if (requests.isEmpty()) {
            return null;
        }
        return requests.poll();
    }

    /**
     * Method to add a storage request to the queue for the warehouse operator.
     * @param request Storage_request to be added
     */
    public void add_request(Storage_request request) {
        requests.add(request);
    }

    // ===== methods for material storage =====

    /**
     * Method to refill the material storage.
     * @param material MaterialType type of material to be added
     * @param amount int amount to be added
     * @return int amount that could not be stored
     */
    public int refill_material_storage(MaterialType material, int amount) {
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
            case CIRCUIT_BOARD_COMPONETS:
                if (storage_circuit_board_components + amount <= storage_kapazitaet) {
                    storage_circuit_board_components += amount;
                    return 0;
                }
                else {
                    int new_amount = storage_kapazitaet - storage_circuit_board_components;
                    storage_circuit_board_components += new_amount;
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
            default:
                return amount;
        }
    }


    /**
     * These methode get plastics from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_plastic(int amount) {
        if (storage_plastic < amount) {
            amount = storage_plastic;
        }
        storage_plastic -= amount;
        return amount;
    }


    /**
     * These methode get circuit board components from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_circuit_board_components(int amount) {
        if (storage_circuit_board_components < amount) {
            amount = storage_circuit_board_components;
        }
        storage_circuit_board_components -= amount;
        return amount;
    }


    /**
     * These methode get displays from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_displays(int amount) {
        if (storage_displays < amount) {
            amount = storage_displays;
        }
        storage_displays -= amount;
        return amount;
    }


    /**
     * These methode get motors from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_motors(int amount) {
        if (storage_motors < amount) {
            amount = storage_motors;
        }
        storage_motors -= amount;
        return amount;
    }


    /**
     * These methode get glue from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_glue(int amount) {
        if (storage_glue < amount) {
            amount = storage_glue;
        }
        storage_glue -= amount;
        return amount;
    }


    /**
     * These methode get packing material from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_storage_packing_material(int amount) {
        if (storage_packing_material < amount) {
            amount = storage_packing_material;
        }
        storage_packing_material -= amount;
        return amount;
    }


    /**
     * These methods check if material storage is available for accessing.
     * @param type MaterialType type of material to be checked
     * @return boolean true if storage is available, false otherwise
     */
    public boolean check_storage_available(MaterialType type) {
        switch (type) {
            case PLASTIC:
                if (storage_plastic_semaphore.tryAcquire()) {
                    return true;
                }
                else
                    return false;
            case CIRCUIT_BOARD_COMPONETS:
                if (storage_circuit_board_components_semaphore.tryAcquire()) {
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


    /**
     * These methods release the material storage semaphore after accessing material storage.
     * @param type MaterialType type of material storage to be released
     */
    public void release_material_storage(MaterialType type) {
        switch (type) {
            case PLASTIC:
                storage_plastic_semaphore.release();
                break;
            case CIRCUIT_BOARD_COMPONETS:
                storage_circuit_board_components_semaphore.release();
                break;
            case DISPLAYS:
                storage_displays_semaphore.release();
                break;
            case MOTORS:
                storage_motors_semaphore.release();
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

    // ===== methods for packets storage =====

    /**
     * These methode receive packets into storage.
     * @param amount int amount to be added to storage
     * @return int amount that could not be stored
     */
    public int receive_packets(int amount) {
        if (storage_packets + amount <= storage_kapazitaet) {
            storage_packets += amount;
            return 0;
        }
        else {
            int new_amount = storage_kapazitaet - storage_packets;
            storage_packets += new_amount;
            return amount - new_amount;
        }
    }


    /**
     * These methode get packets from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_packets(int amount) {
        if (storage_packets < amount) {
            amount = storage_packets;
        }
        storage_packets -= amount;
        return amount;
    }


    /**
     * These methods check if packet storage is available for accessing.
     * @return boolean true if storage is available, false otherwise
     */
    public boolean check_packet_storage_available() {
        if (storage_packets_semaphore.tryAcquire()) {
            return true;
        }
        else
            return false;
    }


    /**
     * These methods release the packet storage semaphore after accessing packet storage.
     */
    public void release_packet_storage() {
        storage_packets_semaphore.release();
    }

    // ===== methods for scrap storage =====

    /**
     * These methode receive scrap into storage.
     * @param amount int amount to be added to storage
     * @return int amount that could not be stored
     */
    public int receive_scrap(int amount) {
        if (storage_scrap + amount <= storage_kapazitaet) {
            storage_scrap += amount;
            return 0;
        }
        else {
            int new_amount = storage_kapazitaet - storage_scrap;
            storage_scrap += new_amount;
            return amount - new_amount;
        }
    }


    /**
     * These methode get scrap from storage.
     * @param amount int amount to be taken from storage
     * @return int amount actually taken from storage
     */
    public int get_scrap(int amount) {
        if (storage_scrap < amount) {
            amount = storage_scrap;
        }
        storage_scrap -= amount;
        return amount;
    }


    /**
     * These methods check if scrap storage is available for accessing.
     * @return boolean true if storage is available, false otherwise
     */
    public boolean check_scrap_storage_available() {
        if (storage_scrap_semaphore.tryAcquire()) {
            return true;
        }
        else
            return false;
    }


    /**
     * These methods release the scrap storage semaphore after accessing scrap storage.
     */
    public void release_scrap_storage() {
        storage_scrap_semaphore.release();
    }
}
