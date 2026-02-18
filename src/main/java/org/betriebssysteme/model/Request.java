package org.betriebssysteme.model;

import org.betriebssysteme.model.cargo.Cargo;

/**
 * The Request record represents a request for cargo made by a station.
 * It contains information about the quantity of cargo requested, the priority level of the request,
 * the type of cargo requested, and the identification number of the station making the request.
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
