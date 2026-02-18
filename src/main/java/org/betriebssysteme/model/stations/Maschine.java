package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Maschine class represents a machine in the production line.
 * It is an abstract class that extends Thread and implements the Station interface.
 * Each Maschine has an identification number, a reference to the next Maschine in the production line,
 * processing time, sleep time, storage capacity, and a storage map to hold the current quantity of each cargo type.
 * The Maschine class also includes methods for running the production cycle,
 * checking storage status, producing products, storing products,
 * delivering products to the next machine, and handling cargo requests and notifications.
 */
public abstract class Maschine extends Thread implements Station{
    protected int identificationNumber;
    protected Maschine nextMaschine;
    protected int timeToProcess;
    protected int timeToSleep;
    protected boolean running;
    protected Status status;
    protected int maxStorageCapacity;
    /**
     * Semaphore to synchronize access to the storage map,
     * which holds the current quantity of each cargo type in the machine's storage.
     * The semaphore is initialized with 1 permit,
     * which means that only one thread can access the storage at a time.
     * When a thread wants to access the storage,
     * it must acquire the semaphore before accessing it and release it afterward.
     * This ensures that when multiple threads (machines) are trying to access the storage concurrently,
     * they do not interfere with each other and cause data corruption or inconsistencies
     */
    protected Semaphore storageSemaphore;
    protected Map<Cargo, Integer> storage;

    protected Map<Cargo, Boolean> requestedCargoTypes;
    protected Cargo productCargo;
    protected Logger logger;
    protected int maschinePriority;
    protected boolean cargoHandoverToNextMaschineInProgress = false;
    /**
     * Semaphore to synchronize access to the cargosOnTransit queue,
     * which holds the cargos that are currently being transferred to the next machine.
     * The semaphore is initialized with 1 permit,
     * which means that only one thread can access the queue at a time.
     * This ensures that when multiple threads (machines) are adding or removing cargos from the queue,
     * they do not interfere with each other and cause data corruption or inconsistencies.
     */
    Semaphore notificationSemaphore = new Semaphore(1);
    protected Queue<Cargo> cargosOnTransit = new LinkedList<>();

    public Maschine(int identificationNumber,
                    int timeToProcess,
                    int timeToSleep,
                    int maxStorageCapacity,
                    Maschine nextMaschine,
                    Map<Cargo, Integer> initialStorage,
                    Cargo productCargo,
                    int maschinePriority){
        this.identificationNumber = identificationNumber;
        this.timeToProcess = timeToProcess;
        this.timeToSleep = timeToSleep;
        this.maxStorageCapacity = maxStorageCapacity;
        this.nextMaschine = nextMaschine;
        this.storage = initialStorage;
        this.storageSemaphore = new Semaphore(1);
        this.status = StatusInfo.OPERATIONAL;
        this.running = true;
        this.productCargo = productCargo;
        this.requestedCargoTypes = new HashMap<>();
        this.maschinePriority = maschinePriority;
        this.logger = LoggerFactory.getLogger("Maschine-" + identificationNumber);
        logger.info("Maschine {} initialized for product: {}", identificationNumber, productCargo);
        setDaemon(true); // Set the thread as a daemon
        // so that it will automatically shut down when the main program exits
    }

    /**
     * The runProductionCycle method represents a single production cycle for the machine.
     * It performs the following steps:
     * 1. Log the start of the production cycle.
     * 2. Check the storage status by calling the checkStorageStatus method.
     * 3. Check if cargo production is possible by calling the checkIfCargoProductionIsPossible method.
     * 4. If the machine is running,
     * it produces a product by calling the produceProduct method and then either stores the product
     * or delivers it to the next machine by calling the storeProductOrDeliverToNextMachine method.
     * 5. If the machine is not running,
     * it sleeps for a specified time to release the CPU and improve performance when the machine is stopped.
     */
    private void runProductionCycle() {
        logger.info("Starting production cycle");
        checkStorageStatus();
        checkIfCargoProductionIsPossible();
        if (running){
            // Only produce if the machine is running,
            // otherwise sleep to release CPU for better performance when stopped
            Cargo producedCargo = produceProduct();
            storeProductOrDeliverToNextMachine(producedCargo);
        }
        else{
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The checkStorageStatus method is an abstract method that must be implemented by subclasses of the Maschine class.
     * It is responsible for checking the storage status of the machine,
     * such as the quantity of each cargo type in the storage
     * and whether there is enough capacity to store more products or scrap.
     */
    protected abstract void checkStorageStatus();

    /**
     * The checkIfCargoProductionIsPossible method is an abstract method
     * that must be implemented by subclasses of the Maschine class.
     * It is responsible for checking if the conditions for producing cargo are met,
     * such as whether the required ingredients are available in the storage
     * and whether there is enough capacity to store the produced cargo.
     */
    protected abstract void checkIfCargoProductionIsPossible();


    /**
     * The produceProduct method is an abstract method that must be implemented by subclasses of the Maschine class.
     * It is responsible for producing a product based on the machine's recipe and production logic.
     * The method should return the produced cargo, which can then be stored or delivered to the next machine.
     *
     * @return The cargo representing the produced product.
     */
    protected abstract Cargo produceProduct();

    /**
     * The storeProductOrDeliverToNextMachine method is an abstract method
     * that must be implemented by subclasses of the Maschine class.
     * It is responsible for either storing the produced product in the machine's storage
     * or delivering it to the next machine in the production line.
     * The implementation of this method will depend on the specific logic and requirements of the machine,
     * such as whether it has a next machine to deliver to or if
     * it needs to store the product due to certain conditions.
     *
     * @param cargo The cargo representing the produced product that needs to be stored or delivered.
     */
    protected abstract void storeProductOrDeliverToNextMachine(Cargo cargo);

    /**
     * The sendCargoRequest method is responsible
     * for sending a request for a specific cargo and quantity to the production headquarters.
     * It checks if a request for the specified cargo has already been sent before to avoid duplicate requests.
     * If a request has not been sent before, it creates a new Request object with the specified quantity,
     * machine priority, cargo, and machine identification number,
     * and adds it to the production headquarters' request queue.
     * @param cargo The cargo for which the request is being sent.
     * @param quantity The quantity of the cargo being requested.
     */
    protected void sendCargoRequest(Cargo cargo, int quantity) {
        boolean requestedBefore = requestedCargoTypes.getOrDefault(cargo, false);
        if (!requestedBefore){
            Request request = new Request(quantity,this.maschinePriority, cargo, this.identificationNumber);
            ProductionHeadquarters.getInstance().addRequest(request);
            requestedCargoTypes.put(cargo, true);
            logger.info("Added request to headquarters for cargo: {} quantity: {}", cargo, quantity);
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled())
            {
                System.out.println("Machine " + identificationNumber + " sent request for cargo: " + cargo + " quantity: " + quantity);
            }
        }
    }

    /**
     * The addCargoRequestNotification method is responsible for adding a notification to the machine's queue
     * when a cargo request has been fulfilled and the cargo is being sent to the next machine.
     * This method is called by the previous machine in the production line
     * to notify the current machine that a cargo is on its way.
     * The method uses semaphore to synchronize access to the cargosOnTransit queue,
     * ensuring that multiple threads do not interfere with each other when adding notifications.
     * @param cargo The cargo that is being sent to the next machine, which will be added to the notification queue.
     */
    public void addCargoRequestNotification(Cargo cargo){
        try {
            notificationSemaphore.acquire();
            cargosOnTransit.add(cargo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            notificationSemaphore.release();
        }
    }

    /**
     * The markRequestAsCompleted method is responsible
     * for marking a cargo request as completed in the machine's requestedCargoTypes map.
     * This method is called by the warehouse clerk
     * after successfully delivering the requested cargo to the machine
     * to reset the request status for that cargo type,
     * allowing the machine to send new requests for that cargo type in the future if needed.
     * @param cargo The cargo for which the request is being marked as completed.
     */
    public void markRequestAsCompleted(Cargo cargo){
        System.out.println("Machine " + identificationNumber + " marked request as completed for cargo: " + cargo);
        requestedCargoTypes.put(cargo, false);
    }

    /**
     * The deliverToNextMachine method is responsible
     * for delivering the produced cargo to the next machine in the production line.
     * It checks
     * if the next machine has remaining storage capacity for the cargo before attempting to deliver it.
     * If the next machine's storage is full,
     * it stops the current machine
     * and retries after a short delay until the next machine has capacity to receive the cargo.
     * @param cargo The cargo representing the produced product that needs to be delivered to the next machine.
     */
    protected void deliverToNextMachine(Cargo cargo) {
        if (nextMaschine != null) {
            boolean cargoNotified = false;
            while (!cargoNotified) {
                try {
                    boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
                    if (!remainingCapacity) {
                        if (running){
                            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                                System.out.println("Machine " + identificationNumber + " stopping as next machine " + nextMaschine.getIdentificationNumber() + " storage full.");
                            }
                            logger.info("Next machine {} storage full, stopping machine {}", nextMaschine.getIdentificationNumber(), identificationNumber);
                            stopMachine();
                        }
                        logger.info("Next machine storage full, retrying in 500ms");
                        Thread.sleep(500);
                    }
                    else {
                        if (!running){
                            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                                System.out.println("Machine " + identificationNumber + " starting as next machine " + nextMaschine.getIdentificationNumber() + " has storage capacity.");
                            }
                            logger.info("Next machine {} has storage capacity, starting machine {}", nextMaschine.getIdentificationNumber(), identificationNumber);
                            startMachine();
                        }
                        notifyNextMaschineOfCargoSending(cargo);
                        cargoNotified = true;
                        cargoHandoverToNextMaschineInProgress = true;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Machine " + identificationNumber + " interrupted while delivering cargo", e);
                }
            }
        }
        else {
            logger.warn("Next machine is null, cannot deliver product");
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("Machine " + identificationNumber + " cannot deliver product as next machine is null.");
            }
        }
    }

    /**
     * The storeProduct method is responsible for storing the produced cargo in the machine's storage.
     * It uses semaphore to synchronize access to the storage map,
     * ensuring that multiple threads do not interfere with each other when updating the storage.
     * The method checks if the cargo type already exists in the storage
     * and if there is capacity to store more of that
     * cargo type before updating the quantity in the storage.
     * @param cargo The cargo representing the produced product that needs to be stored in the machine's storage.
     */
    protected void storeProduct(Cargo cargo) {
        try {
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity < maxStorageCapacity) {
                    storage.put(cargo, currentQuantity + 1);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Stored product in machine storage: {}", cargo);
            storageSemaphore.release();
        }
    }

    /**
     * The stopMachine method is responsible for stopping the machine by setting the running flag to false.
     * It also logs the action
     * and prints a message to the console if console output is enabled in the production headquarters.
     */
    public void stopMachine() {
        running = false;
        logger.debug("Stopping machine{}", identificationNumber);
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("Machine " + identificationNumber + " stopped.");
        }
    }

    /**
     * The startMachine method is responsible for starting the machine by setting the running flag to true.
     * It also logs the action
     * and prints a message to the console if console output is enabled in the production headquarters.
     */
    public void startMachine() {
        running = true;
        logger.debug("Starting machine{}", identificationNumber);
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("Machine " + identificationNumber + " started.");
        }
    }

    //============================================================================
    // Getters and Setters
    public boolean isRunning() {
        return running;
    }

    public void setNextMaschine(Maschine nextMaschine) {
        this.nextMaschine = nextMaschine;
    }

    /**
     * The getCargoHandoverToNextMaschineInProgress method is responsible for checking
     * if a cargo handover to the next machine is currently in progress.
     * It returns true if a cargo handover is in progress and resets the flag to false,
     * indicating that the handover has been acknowledged.
     * @return true if a cargo handover to the next machine is in progress, false otherwise.
     */
    public boolean getCargoHandoverToNextMaschineInProgress(){
        boolean cargoHandoverToNextMaschineInProgressCopy = cargoHandoverToNextMaschineInProgress;
        if (cargoHandoverToNextMaschineInProgressCopy){
            cargoHandoverToNextMaschineInProgress = false;
        }
        return cargoHandoverToNextMaschineInProgressCopy;
    }

    /**
     * The getRemainingStorageCapacity method is responsible
     * for checking if the next machine has remaining storage capacity for a specific cargo type.
     * It calculates the remaining capacity by acquiring the storage semaphore to safely access the storage map
     * and then checking the current quantity of the specified cargo type.
     * It also accounts for any cargos
     * that are currently in transit to the next machine by acquiring the notification semaphore
     * and checking the cargosOnTransit queue.
     * The method returns true if there is remaining storage capacity for the specified cargo type,
     * and false otherwise.
     * @param cargo The cargo type for which to check the remaining storage capacity in the next machine.
     * @return true if there is remaining storage capacity for the specified cargo type in the next machine, false otherwise.
     */
    public boolean getRemainingStorageCapacity(Cargo cargo){
        int remainingCapacity;
        try {
            storageSemaphore.acquire();
            int currentQuantity = storage.getOrDefault(cargo, 0);
            remainingCapacity = maxStorageCapacity - currentQuantity;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            storageSemaphore.release();
        }
        try {
            notificationSemaphore.acquire();
            for (Cargo c : cargosOnTransit){
                if (c.equals(cargo)){
                    remainingCapacity -= 1;
                }
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            notificationSemaphore.release();
        }
        return remainingCapacity > 0;
    }


    /**
     * The notifyNextMaschineOfCargoSending method is responsible
     * for notifying the next machine in the production line that a cargo is being sent to it.
     * It adds a notification to the next machine's queue
     * by calling the addCargoRequestNotification method on the next machine.
     * This allows the next machine to be aware of incoming cargos and prepare to receive them,
     * ensuring smooth coordination between machines in the production line.
     * @param cargo The cargo that is being sent to the next machine, which will be included in the notification.
     */
    protected void notifyNextMaschineOfCargoSending(Cargo cargo){
        if (nextMaschine != null) {
            nextMaschine.addCargoRequestNotification(cargo);
            logger.info("Notified next machine {} of cargo sending: {}", nextMaschine.getIdentificationNumber(), cargo);
        }
    }

    /**
     * The notifyMachineCargoHandoverCompleted method is responsible
     * for handling the notification that a cargo handover to the next machine has been completed.
     * It removes the cargo from the cargosOnTransit queue
     * and updates the machine's storage by calling the resiveCargo method to reflect
     * that the cargo has been successfully received by the next machine.
     * If there are still cargos in transit after processing the notification,
     * it logs a warning indicating that there are still cargos in progress,
     * which can help identify potential issues in the production line coordination.
     * The method uses semaphores to synchronize access to the cargosOnTransit queue and the storage map,
     * ensuring thread safety when updating the machine's state based on the cargo handover notifications.
     */
    public void notifyMachineCargoHandoverCompleted(){
        Cargo cargo;
        try {
            notificationSemaphore.acquire();
            cargo = cargosOnTransit.poll();
            if (cargo == null){
                logger.warn("Machine {} received notification of cargo handover completed but no cargo found on transit.", identificationNumber);
                if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                    System.out.println("Machine " + identificationNumber + " received notification of cargo handover completed but no cargo found on transit.");
                }
                return;
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            notificationSemaphore.release();
        }
        int warningQuantity = resiveCargo(cargo, 1);
        if (warningQuantity > 0){
            logger.warn("Machine {} received notification of cargo handover completed for cargo: {} but still has {} cargo in progress.", identificationNumber, cargo, warningQuantity);
            if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
                System.out.println("Machine " + identificationNumber + " received notification of cargo handover completed for cargo: " + cargo + " but still has " + warningQuantity + " cargo in progress.");
            }
        }
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        logger.info("Starting thread");
        while (true) {
            runProductionCycle();
            try {
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ============================================================================
    //Station methods
    @Override
    public int resiveCargo(Cargo cargo, int quantity) {
        try{
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity + quantity <= maxStorageCapacity) {
                    storage.put(cargo, currentQuantity + quantity);
                    return quantity;
                } else {
                    int acceptedQuantity = maxStorageCapacity - currentQuantity;
                    storage.put(cargo, maxStorageCapacity);
                    return acceptedQuantity;
                }
            }
            else {
                return 0;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Machine "+ identificationNumber + " interrupted while receiving cargo", e);
        } finally {
            logger.info("Stored product in machine storage: {}", cargo);
            storageSemaphore.release();
        }
    }

    @Override
    public int handOverCargo(Cargo cargo, int quantity) {
        try{
            storageSemaphore.acquire();
            if (storage.containsKey(cargo)) {
                int currentQuantity = storage.getOrDefault(cargo, 0);
                if (currentQuantity >= quantity) {
                    storage.put(cargo, currentQuantity - quantity);
                    return quantity;
                } else {
                    storage.put(cargo, 0);
                    return currentQuantity;
                }
            }
            else {
                logger.debug("Requested cargo not available in storage: {}", cargo);
                return 0;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Handed over product from machine storage: {} quantity: {}", cargo, quantity);
            storageSemaphore.release();
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }


    @Override
    public int getIdentificationNumber() {
        return identificationNumber;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public String [][] getInfoArray(){
        String [][] infoArray = new String[storage.size() + 8][2];

        String cargoLabel;
        if (ProductionHeadquarters.getInstance().getStations().get(identificationNumber) instanceof ProductionMaschine){
            cargoLabel = "Produced Cargo";
        }
        else if (ProductionHeadquarters.getInstance().getStations().get(identificationNumber) instanceof PackagingMaschine){
            cargoLabel = "Packaged Cargo";
        }
        else {
            cargoLabel = "Controlled Cargo";
        }

        int index = 0;
        infoArray[index++] = new String[]{"Maschine ID", String.valueOf(identificationNumber)};
        infoArray[index++] = new String[]{cargoLabel, String.valueOf(productCargo)};
        infoArray[index++] = new String[]{"Status", String.valueOf(status)};
        infoArray[index++] = new String[]{"Running", String.valueOf(running)};
        infoArray[index++] = new String[]{"Max Storage Capacity", String.valueOf(maxStorageCapacity)};
        infoArray[index++] = new String[]{"Next Maschine ID", nextMaschine != null ? String.valueOf(nextMaschine.getIdentificationNumber()) : "None"};
        infoArray[index++] = new String[]{"Time To Process (ms)", String.valueOf(timeToProcess)};
        infoArray[index++] = new String[]{"Storage", "Quantity"};

        for (Map.Entry<Cargo, Integer> entry : storage.entrySet()) {
            infoArray[index++] = new String[]{entry.getKey().toString(), String.valueOf(entry.getValue())};
        }
        return infoArray;
    }
}
