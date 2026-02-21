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
 * The abstract class Maschine represents a machine in the production line.
 * It extends the Thread class to run as a separate thread
 * and implements the Station interface to set the rules for receiving and handing over cargo.
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
     * so that only one thread can access the storage at a time.
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

    /**
     * Constructor for the Maschine class.
     * @param identificationNumber The identification number for the machine.
     * @param timeToProcess The time in milliseconds that the machine takes to process a product.
     * @param timeToSleep The time in milliseconds that the machine will sleep between production cycles.
     * @param maxStorageCapacity The maximum storage capacity for the machine.
     * @param nextMaschine The next machine in the production line.
     * @param initialStorage A Map containing the initial quantity of each cargo type in storage when the machine is created.
     * @param productCargo The type of product cargo that the machine produces.
     * @param maschinePriority The priority level of the machine for requests
     */
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
     * The runProductionCycle method is executing one cycle of the machine's production process.
     * It checks the storage status and whether cargo production is possible based on the machine's logic.
     * If the machine is running, it produces a product and either stores it or delivers it to the next machine.
     * If the machine is not running, it sleeps for a specified time.
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
     * It is checking the current status of the machine's storage
     * and updating the machine's status.
     */
    protected abstract void checkStorageStatus();

    /**
     * The checkIfCargoProductionIsPossible method is an abstract method that must be implemented by subclasses of the Maschine class.
     * It is checking if the machine can produce its product and updating the running status of the machine.
     */
    protected abstract void checkIfCargoProductionIsPossible();


    /**
     * The produceProduct method is an abstract method that must be implemented by subclasses of the Maschine class.
     * It is producing the product cargo according to the machine's logic.
     * @return the produced cargo
     */
    protected abstract Cargo produceProduct();

    /**
     * The storeProductOrDeliverToNextMachine method is an abstract method that must be implemented by subclasses of the Maschine class.
     * It stores the produced product in the machine's storage or delivers it to the next machine in the production line according to the machine's logic.
     */
    protected abstract void storeProductOrDeliverToNextMachine(Cargo cargo);

    /**
     * The sendCargoRequest method is responsible for sending a cargo request to the production headquarters
     * when the machine needs a delivery of material cargo or an empting of product cargo to continue production.
     * If a request for the same cargo type has already been sent and is still pending,
     * it will not send another request for the same cargo type to avoid duplicate requests.
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
     * The addCargoTransitNotification method is
     * to inform the maschine that a cargo is being sent from the previous machine to it.
     * It adds the cargo to the cargosOnTransit queue to keep track of incoming cargos
     * and uses semaphore to synchronize access to the queue,
     * ensuring thread safety when multiple threads (machines) are adding notifications concurrently.
     * (Only one thread can access the queue at a time.)
     * @param cargo the cargo on transit
     */
    public void addCargoTransitNotification(Cargo cargo){
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
     * The markRequestAsCompleted method marks a request for a specific cargo type as completed by updating the requestedCargoTypes map.
     * So that the machine can send new requests for that cargo type in the future when needed.
     * @param cargo The cargo for which the request is being marked as completed.
     */
    public void markRequestAsCompleted(Cargo cargo){
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("Machine " + identificationNumber + " marked request as completed for cargo: " + cargo);
        }
        logger.info("Marked request as completed for cargo: {}", cargo);
        requestedCargoTypes.put(cargo, false);
    }

    /**
     * The deliverToNextMachine method is delivering the produced cargo to the next machine in the production line.
     * If the next maschine can receive the cargo, it will notify the next machine of the incoming cargo and
     * set a flag to indicate that a cargo handover to the next machine is in progress.
     * If the next machine's storage is full and cannot receive the cargo,
     * it will stop the machine and retry the delivery until it goes through.
     * @param cargo the cargo to be delivered to the next machine
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
                        logger.info("Next machine storage full, retrying in {}ms", timeToSleep);
                        Thread.sleep(timeToSleep);
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
     * The storeProduct method is storing the produced product in the machine's storage.
     * It uses semaphore to synchronize access to the storage map,
     * ensuring that only one thread can access the storage at a time to prevent data corruption.
     * @param cargo the product to be delivered
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
     */
    public void stopMachine() {
        running = false;
        logger.debug("Stopping machine{}", identificationNumber);
        if (ProductionHeadquarters.getInstance().isConsoleOutputEnabled()) {
            System.out.println("Machine " + identificationNumber + " stopped.");
        }
    }

    /**
     * The stopMachine method is responsible for stopping the machine by setting the running flag to false.
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
     * The getCargoHandoverToNextMaschineInProgress method is a getter for the cargoHandoverToNextMaschineInProgress flag,
     * which indicates whether a cargo handover to the next machine is currently in progress.
     * On a call to this method, it returns the current value of the flag and then resets it to false.
     */
    public boolean getCargoHandoverToNextMaschineInProgress(){
        boolean cargoHandoverToNextMaschineInProgressCopy = cargoHandoverToNextMaschineInProgress;
        if (cargoHandoverToNextMaschineInProgressCopy){
            cargoHandoverToNextMaschineInProgress = false;
        }
        return cargoHandoverToNextMaschineInProgressCopy;
    }

    /**
     * The getRemainingStorageCapacity method checks
     * if there is remaining storage capacity for a specific cargo type in the machine's storage.
     * The method uses semaphores to synchronize access to the storage map and the cargosOnTransit queue,
     * ensuring thread safety when checking the storage capacity.
     * @param cargo the cargo type for which to check the remaining storage capacity
     * @return a boolean value whether there is remaining storage capacity or not
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
     * The notifyNextMaschineOfCargoSending method is responsible for notifying the next machine in the production line that a cargo is being sent to it.
     * @param cargo the cargo that is being sent to the next machine
     */
    protected void notifyNextMaschineOfCargoSending(Cargo cargo){
        if (nextMaschine != null) {
            nextMaschine.addCargoTransitNotification(cargo);
            logger.info("Notified next machine {} of cargo sending: {}", nextMaschine.getIdentificationNumber(), cargo);
        }
    }

    /**
     * The notifyMachineCargoHandoverCompleted method is responsible for notifying the machine that a cargo handover from the previous machine has been completed.
     * It called by the GUI when the cargo handover animation is completed to inform the machine that the cargo has arrived and can be processed.
     * The method uses semaphores to synchronize access to the cargosOnTransit queue,
     * ensuring thread safety when multiple threads (machines) are adding or removing cargos from the queue concurrently.
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
        resiveCargo(cargo, 1);
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
