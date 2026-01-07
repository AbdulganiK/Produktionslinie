package org.betriebssysteme.model;

import java.util.Map;

public interface Station {
    int resiveCargo(Cargo cargo, int quantity);
    int collectCargo(Cargo cargo, int quantity);
    Status getStatus();
    Map getInformationMap();
    int getId();
}
