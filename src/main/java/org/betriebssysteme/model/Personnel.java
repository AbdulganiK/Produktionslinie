package org.betriebssysteme.model;

import java.util.Map;

public interface Personnel {
    int resiveCargo(Cargo cargo, int quantity);
    int collectCargo(Cargo cargo, int quantity);
    Status getStatus();
    Map getInformationMap();
    int getId();
    int getDestinationStationId();
    int getOriginStationId();
    Task getCurrentTask();
}
