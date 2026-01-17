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

import java.util.HashMap;
import java.util.Map;

public class WarehouseClerk extends Thread implements Personnel {
    private Cargo cargo;
    private Status status;
    private int identificationNumber;
    private int originStationId;
    private int destinationStationId;
    private Task task;
    private int timeForTravel_ms;
    private int timeForTask_ms;
    private int timeForSleep_ms;
    private Request currentRequest;
    private Logger logger;
    private int idOfCurrentDestinationStation;
    private boolean ready = false;

    /**
     * Constructor for WarehouseClerk.
     * @param identificationNumber ID of the WarehouseClerk
     * @param timeForTravel_ms time taken to travel between stations in milliseconds
     * @param timeForTask_ms time taken to perform tasks (collecting/delivering cargo) in milliseconds
     * @param timeForSleep_ms time taken to sleep between task cycles in milliseconds
     */
    public WarehouseClerk(int identificationNumber,
                          int timeForTravel_ms,
                          int timeForTask_ms,
                          int timeForSleep_ms) {
        this.identificationNumber = identificationNumber;
        this.timeForTravel_ms = timeForTravel_ms;
        this.timeForTask_ms = timeForTask_ms;
        this.timeForSleep_ms = timeForSleep_ms;
        this.status = StatusWarning.STOPPED;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.task = Task.JOBLESS;
        this.logger = org.slf4j.LoggerFactory.getLogger("WarehouseClerk-" + identificationNumber);
    }

    /**
     * Runs a single task cycle for the WarehouseClerk.
     */
    private void runTaskCycle() {
        boolean hasRequest = getRequested();
        if (!hasRequest) {
            status = StatusWarning.STOPPED;
            task = Task.JOBLESS;
            return;
        }
        else {
            try {
                // Travel to origin station
                idOfCurrentDestinationStation = originStationId;
                status = StatusInfo.TRAVEL_TO_STATION;
                //awaitReady(); TODO: Implement ready check if needed
                Thread.sleep(timeForTravel_ms);

                // Collect cargo from origin station
                status = StatusInfo.COLLECT_CARGO;
                int transportedQuantity = collectCargo(cargo, currentRequest.quantity());
                Thread.sleep(timeForTask_ms);

                // Travel to destination station
                status = StatusInfo.TRANSPORT_CARGO;
                idOfCurrentDestinationStation = destinationStationId;
                //awaitReady(); TODO: Implement ready check if needed
                Thread.sleep(timeForTravel_ms);

                // Deliver cargo to destination station
                status = StatusInfo.DELIVER_CARGO;
                refillCargo(cargo, transportedQuantity);
                Thread.sleep(timeForTask_ms);

                // Mark request as completed
                Maschine requestedMachine = (Maschine) ProductionHeadquarters.getInstance().getStations().get(currentRequest.stationId());
                requestedMachine.markRequestAsCompleted(cargo);
                logger.info("WarehouseClerk " + identificationNumber + " completed request for " + cargo + " at Station " + currentRequest.stationId());
                System.out.println("WarehouseClerk " + identificationNumber + " completed request for " + cargo + " at Station " + currentRequest.stationId());

                // Travel back to headquarters
                status = StatusInfo.TRAVEL_TO_HEADQUARTERS;
                idOfCurrentDestinationStation = 0; // Headquarters station ID
                //awaitReady(); TODO: Implement ready check if needed
                Thread.sleep(timeForTravel_ms);
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
                originStationId = 1; // Headquarters station ID
                destinationStationId = currentRequest.stationId();
                logger.info("WarehouseClerk " + identificationNumber + " received a request to deliver MATERIAL " + cargo + " to Station " + currentRequest.stationId());
            } else if (requestedCargoTyp == CargoTyp.PRODUCT) {
                task = Task.EMPTYING;
                originStationId = currentRequest.stationId();
                destinationStationId = 1; // Headquarters station ID
                logger.info("WarehouseClerk " + identificationNumber + " received a request to collect PRODUCT " + cargo + " from Station " + currentRequest.stationId());
            }
            return true;
        }
        return false;
    }

    private synchronized void awaitReady() throws InterruptedException {
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
    public Map getInformationMap() {
        return null;
    }

    @Override
    public int getDestinationStationId() {
        return destinationStationId;
    }

    @Override
    public int getOriginStationId() {
        return originStationId;
    }

    @Override
    public Task getCurrentTask() {
        return task;
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
        String[][] infoArray = new String[10][2];
        infoArray[0][0] = "Identification Number";
        infoArray[0][1] = String.valueOf(identificationNumber);

        infoArray[1][0] = "Status";
        infoArray[1][1] = status.toString();

        infoArray[2][0] = "Current Task";
        infoArray[2][1] = task.toString();

        infoArray[3][0] = "Origin Station ID";
        infoArray[3][1] = String.valueOf(originStationId);

        infoArray[4][0] = "Destination Station ID";
        infoArray[4][1] = String.valueOf(destinationStationId);

        infoArray[5][0] = "Cargo";
        if (cargo != null) {
            infoArray[5][1] = cargo.toString();
        } else {
            infoArray[5][1] = "N/A";
        }

        infoArray[6][0] = "Cargo Quantity";
        if (currentRequest != null) {
            infoArray[6][1] = String.valueOf(currentRequest.quantity());
        } else {
            infoArray[6][1] = "N/A";
        }

        infoArray[7][0] = "Time for Travel (ms)";
        infoArray[7][1] = String.valueOf(timeForTravel_ms);

        infoArray[8][0] = "Time for Task (ms)";
        infoArray[8][1] = String.valueOf(timeForTask_ms);

        infoArray[9][0] = "Time for Sleep (ms)";
        infoArray[9][1] = String.valueOf(timeForSleep_ms);
        return infoArray;
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
