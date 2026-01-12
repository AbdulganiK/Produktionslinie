package org.betriebssysteme.model.stations;

import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.Status;
import org.betriebssysteme.model.cargo.Cargo;

import java.util.Map;

public interface Station{
    /**
     * Receive cargo into the station's storage.
     * @param cargo The cargo to be received.
     * @param quantity The quantity of cargo to be received.
     * @return The actual quantity of cargo received.
     */
    int resiveCargo(Cargo cargo, int quantity);

    /**
     * Hand over cargo from the station's storage.
     * @param cargo The cargo to be handed over.
     * @param quantity The quantity of cargo to be handed over.
     * @return The actual quantity of cargo handed over.
     */
    int handOverCargo(Cargo cargo, int quantity);

    /**
     * Get the current status of the station.
     * @return The status of the station.
     */
    Status getStatus();

    /**
     * Get a map containing information about the station.
     * @return A map with station information.
     */
    Map getInformationMap();

    /**
     * Get the identification number of the station.
     * @return The identification number.
     */
    int getIdentificationNumber();

    void start();
}
