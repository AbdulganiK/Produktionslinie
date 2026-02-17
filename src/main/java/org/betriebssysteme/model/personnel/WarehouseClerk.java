package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.stations.Maschine;
import org.betriebssysteme.model.stations.Station;
import org.betriebssysteme.model.status.Status;
import org.betriebssysteme.model.status.StatusInfo;
import org.betriebssysteme.model.status.StatusWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarehouseClerk extends Thread implements Personnel {
    private Cargo cargo;
    private Status status;
    private final int identificationNumber;
    private int originStationId;
    private int destinationStationId;
    private Task task;
    private final int timeForTask_ms;
    private final int timeForSleep_ms;
    private Request currentRequest;
    private final Logger logger;
    private int idOfCurrentDestinationStation;
    private boolean ready = false;
    private final int maxCargoCapacity;
    private final int mainDepotId;
    private final int headquartersId;

    /**
     * Constructor for WarehouseClerk.
     * @param identificationNumber ID of the WarehouseClerk
     * @param timeForTask_ms time taken to perform tasks (collecting/delivering cargo) in milliseconds
     * @param timeForSleep_ms time taken to sleep between task cycles in milliseconds
     */
    public WarehouseClerk(int identificationNumber,
                          int timeForTask_ms,
                          int timeForSleep_ms,
                          int maxCargoCapacity,
                          int mainDepotId) {
        this.identificationNumber = identificationNumber;
        this.timeForTask_ms = timeForTask_ms;
        this.timeForSleep_ms = timeForSleep_ms;
        this.status = StatusWarning.STOPPED;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.task = Task.JOBLESS;
        this.mainDepotId = mainDepotId;
        this.headquartersId = 0;
        this.maxCargoCapacity = maxCargoCapacity;
        this.logger = LoggerFactory.getLogger("WarehouseClerk-" + identificationNumber);
        logger.info("WarehouseClerk " + identificationNumber +" ms, task time: " + timeForTask_ms + " ms, sleep time: " + timeForSleep_ms + " ms.");
    }

    /**
     * Runs a single task cycle for the WarehouseClerk.
     */
    private void runTaskCycle() {
        boolean hasRequest = getRequested();
        if (!hasRequest) {
            status = StatusWarning.STOPPED;
            task = Task.JOBLESS;
        }
        else {
            try {
                // Travel to origin station
                idOfCurrentDestinationStation = originStationId;
                status = StatusInfo.TRAVEL_TO_STATION;
                awaitReady();

                // Collect cargo from origin station
                status = StatusInfo.COLLECT_CARGO;
                int transportedQuantity = 0;
                if (maxCargoCapacity <= currentRequest.quantity()) {
                    transportedQuantity = collectCargo(cargo, maxCargoCapacity);
                }
                else{
                    transportedQuantity = collectCargo(cargo, maxCargoCapacity);
                }
                Thread.sleep(timeForTask_ms);

                // Travel to destination station
                status = StatusInfo.TRANSPORT_CARGO;
                idOfCurrentDestinationStation = destinationStationId;
                awaitReady();

                // Deliver cargo to destination station
                status = StatusInfo.DELIVER_CARGO;
                refillCargo(cargo, transportedQuantity);
                Thread.sleep(timeForTask_ms);

                // Mark request as completed
                Maschine requestedMachine = (Maschine) ProductionHeadquarters.getInstance().getStations().get(currentRequest.stationId());
                requestedMachine.markRequestAsCompleted(cargo);
                logger.info("WarehouseClerk " + identificationNumber + " completed request for " + cargo + " at Station " + currentRequest.stationId());

                // Travel back to headquarters
                status = StatusInfo.TRAVEL_TO_HEADQUARTERS;
                idOfCurrentDestinationStation = headquartersId; // Headquarters station ID
                awaitReady();
            } catch (InterruptedException e) {
                status = StatusWarning.STOPPED;
                throw new RuntimeException(e);
            }
        }
    }

    private boolean getRequested() {
        currentRequest = ProductionHeadquarters.getInstance().pollRequest();
        if (currentRequest != null) {
            CargoTyp requestedCargoTyp = currentRequest.cargo().getCargoTyp();
            cargo = currentRequest.cargo();
            if (requestedCargoTyp == CargoTyp.MATERIAL) {
                task = Task.DELIVERING;
                originStationId = mainDepotId; // Headquarters station ID
                destinationStationId = currentRequest.stationId();
                logger.info("WarehouseClerk " + identificationNumber + " received a request to deliver MATERIAL " + cargo + " to Station " + currentRequest.stationId());
            } else if (requestedCargoTyp == CargoTyp.PRODUCT) {
                task = Task.EMPTYING;
                originStationId = currentRequest.stationId();
                destinationStationId = mainDepotId; // Headquarters station ID
                logger.info("WarehouseClerk " + identificationNumber + " received a request to collect PRODUCT " + cargo + " from Station " + currentRequest.stationId());
            }
            return true;
        }
        return false;
    }

    private synchronized void awaitReady() throws InterruptedException {
        ready = false;
        while (!ready) {
            wait();
        }
    }


    //============================================================================
    // Methods of Personnel interface
    @Override
    public synchronized void setReady() {
        ready = true;
        notifyAll();
    }

    @Override
    public int refillCargo(Cargo cargo, int quantity) {
        int refilled = 0;
        Station destinationStation = (Station) ProductionHeadquarters.getInstance().getStations().get(destinationStationId);
        if (destinationStation == null){
            logger.warn("WarehouseClerk " + identificationNumber + " has no valid destination for the destination station ID: " + destinationStationId);
            return 0;
        }
        refilled = destinationStation.resiveCargo(cargo, quantity);
        return refilled;
    }

    @Override
    public int collectCargo(Cargo cargo, int quantity) {
        int collected = 0;
        Station originStation = (Station) ProductionHeadquarters.getInstance().getStations().get(originStationId);
        if (originStation == null){
            logger.warn("WarehouseClerk " + identificationNumber + " has no valid origin for the origin station ID: " + originStationId);
            return 0;
        }
        collected = originStation.handOverCargo(cargo, quantity);
        return collected;
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
    public String[][] getInfoArray() {
        return new String[][]{
            {"Identification Number", String.valueOf(identificationNumber)},
            {"Status", String.valueOf(status)},
            {"Current Task", String.valueOf(task)},
            {"Origin Station ID", String.valueOf(originStationId)},
            {"Destination Station ID", String.valueOf(destinationStationId)},
            {"Cargo", cargo != null ? cargo.toString() : "N/A"},
            {"Cargo Quantity", currentRequest != null ? String.valueOf(currentRequest.quantity()) : "N/A"},
            {"Time for Task (ms)", String.valueOf(timeForTask_ms)},
            {"Time for Sleep (ms)", String.valueOf(timeForSleep_ms)}
        };
    }

    @Override
    public int getIdOfDestinationStation() {
        return idOfCurrentDestinationStation;
    }

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        while (true) {
            runTaskCycle();

            try {
                Thread.sleep(timeForSleep_ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
