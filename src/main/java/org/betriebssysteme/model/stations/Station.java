package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.Map;

public interface Station {
    int resiveCargo(Cargo cargo, int quantity);
    int handOverCargo(Cargo cargo, int quantity);
    Status getStatus();
    Map getInformationMap();
    int getIdentificationNumber();
}
