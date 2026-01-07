package org.betriebssysteme.model;

import org.betriebssysteme.model.cargo.Cargo;

public record Request(
    int id,
    int quantity,
    int priority,
    Cargo cargo,
    int stationId
) {
}
