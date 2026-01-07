package org.betriebssysteme.model;

public record Request(
    int id,
    int quantity,
    int priority,
    Cargo cargo,
    int stationId
) {
}
