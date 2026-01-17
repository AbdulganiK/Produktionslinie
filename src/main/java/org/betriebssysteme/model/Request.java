package org.betriebssysteme.model;

import org.betriebssysteme.model.cargo.Cargo;

/**
 * Represents a request for cargo at a station.
 *
 * @param quantity   The quantity of cargo requested.
 * @param priority   The priority level of the request.
 * @param cargo      The type of cargo requested.
 * @param stationId  The identification number of the station making the request.
 */
public record Request(
    int quantity,
    int priority,
    Cargo cargo,
    int stationId
) {
}
