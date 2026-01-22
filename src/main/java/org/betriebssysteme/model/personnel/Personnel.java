package org.betriebssysteme.model.personnel;

import org.betriebssysteme.model.Task;
import org.betriebssysteme.model.cargo.Cargo;
import org.betriebssysteme.model.status.Status;

import java.util.Map;

public interface Personnel extends Runnable {
    /**
     * The Method to refill is used to refill cargo at a station.
     * @param cargo The type of cargo to be refilled.
     * @param quantity The quantity of cargo to be refilled.
     * @return The actual quantity of cargo refilled.
     */
    int refillCargo(Cargo cargo, int quantity);
    /**
     * The Method to collect is used to collect cargo at a station.
     * @param cargo The type of cargo to be collected.
     * @param quantity The quantity of cargo to be collected.
     * @return The actual quantity of cargo collected.
     */
    int collectCargo(Cargo cargo, int quantity);
    /**
     * Get the current Status of the Personnel.
     * @return The Status of the Personnel as an enum value.
     */
    Status getStatus();
    /**
     * Get the identification number of the Personnel.
     * @return The identification number as an int.
     */
    int getIdentificationNumber();
    /**
     * Start the Personnel's operations.
     * Personals are implemented as threads and for an easy start this method is provided.
     */
    void start();
    /**
     * Get an array of information about the Personnel.
     * @return A 2D array containing Personnel information.
     */
    String[][] getInfoArray();
    /**
     * Get the Id of the current Destination Station of the Personnel.
     * @return The identification number of the Destination Station as an int.
     */
    int getIdOfDestinationStation();
    /**
     * Set the Personnel as ready for the next sequence of operations.
     * Is used to synchronize the Personnel threads with the Frontend updates.
     */
    void setReady();
}
