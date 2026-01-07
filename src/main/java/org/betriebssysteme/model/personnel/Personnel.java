package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.Map;

public interface Personnel {
    int refillCargo(Cargo cargo, int quantity);
    int collectCargo(Cargo cargo, int quantity);
    Status getStatus();
    Map getInformationMap();
    int getDestinationStationId();
    int getOriginStationId();
    Task getCurrentTask();
    int getIdentificationNumber();
}
