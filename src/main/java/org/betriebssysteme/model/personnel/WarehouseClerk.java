package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Request;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.cargo.CargoTyp;
import org.betriebssysteme.model.stations.Maschine;
import org.betriebssysteme.model.stations.Station;

import java.util.Map;

public class WarehouseClerk extends Thread implements Personnel {
    private Cargo cargo;
    private Status status;
    private int identificationNumber;
    private int originStationId;
    private int destinationStationId;
    private Map<Integer, Station> stations;
    private Task task;
    private int timeForTravel_ms;
    private int timeForTask_ms;
    private int timeForSleep_ms;
    private Request currentRequest;
    private ProductionHeadquarters productionHeadquarters;

    public WarehouseClerk(int identificationNumber,
                          int timeForTravel_ms,
                          int timeForTask_ms,
                          int timeForSleep_ms,
                          ProductionHeadquarters productionHeadquarters) {
        this.identificationNumber = identificationNumber;
        this.stations = productionHeadquarters.getStations();
        this.timeForTravel_ms = timeForTravel_ms;
        this.timeForTask_ms = timeForTask_ms;
        this.timeForSleep_ms = timeForSleep_ms;
        this.status = Status.STOPPED;
        this.originStationId = -1;
        this.destinationStationId = -1;
        this.task = Task.JOBLESS;
        this.productionHeadquarters = productionHeadquarters;
    }

    private void updateStationsMap() {
        this.stations = productionHeadquarters.getStations();
        System.out.println("WarehouseClerk " + identificationNumber + " updated stations map.");
    }

    private void runTaskCycle() {
        boolean hasRequest = getRequested();
        if (!hasRequest) {
            status = Status.STOPPED;
            task = Task.JOBLESS;
            return;
        }
        else {
            status = Status.OPERATING;
            int transportedQuantity = collectCargo(cargo, currentRequest.quantity());
            try {
                Thread.sleep(timeForTravel_ms + timeForTask_ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            refillCargo(cargo, transportedQuantity);
            try {
                Thread.sleep(timeForTravel_ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Maschine requestedMachine = (Maschine) stations.get(currentRequest.stationId());
            requestedMachine.markRequestAsCompleted(cargo);
            System.out.println("WarehouseClerk " + identificationNumber + " completed request for " + cargo + " at Station " + currentRequest.stationId());
        }
    }

    private boolean getRequested() {
        currentRequest = productionHeadquarters.pollRequest();
        if (currentRequest != null) {
            System.out.println("WarehouseClerk " + identificationNumber + " received request for " + currentRequest.cargo() + " at Station " + currentRequest.stationId());
            CargoTyp requestedCargoTyp = currentRequest.cargo().getCargoTyp();
            cargo = currentRequest.cargo();
            if (requestedCargoTyp == CargoTyp.MATERIAL) {
                task = Task.DELIVERING;
                originStationId = -1;
                destinationStationId = currentRequest.stationId();
            } else if (requestedCargoTyp == CargoTyp.PRODUCT) {
                task = Task.EMPTYING;
                originStationId = currentRequest.stationId();
                destinationStationId = -1;
            }
            return true;
        }
        return false;
    }

    public void setProductionHeadquarters(ProductionHeadquarters productionHeadquarters) {
        this.productionHeadquarters = productionHeadquarters;
    }


    //============================================================================
    // Methods of Personnel interface
    @Override
    public int refillCargo(Cargo cargo, int quantity) {
        int refilled = 0;
        Station destinationStation = stations.get(destinationStationId);
        if (destinationStation == null){
            System.out.println("WarehouseClerk " + identificationNumber + " has no valid destination station to refill cargo.");
            return 0;
        }
        refilled = destinationStation.resiveCargo(cargo, quantity);
        return refilled;
    }

    @Override
    public int collectCargo(Cargo cargo, int quantity) {
        int collected = 0;
        Station originStation = stations.get(originStationId);
        if (originStation == null){
            System.out.println("WarehouseClerk " + identificationNumber + " has no valid origin station to collect cargo.");
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
        return Map.of();
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

    // ============================================================================
    //Thread methods
    @Override
    public void run() {
        updateStationsMap();
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
