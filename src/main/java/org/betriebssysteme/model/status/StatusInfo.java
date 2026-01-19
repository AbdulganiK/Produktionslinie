package org.betriebssysteme.model.status;

/**
 * Enum representing informational status types.
 */
public enum StatusInfo implements Status {
    PRODUCING,
    STORE_PRODUCT,
    HAND_OVER_PRODUCT,
    COLLECT_CARGO,
    DELIVER_CARGO,
    TRANSPORT_CARGO,
    TRAVEL_TO_HEADQUARTERS,
    CHECK_STORAGE,
    OPERATIONAL,
    TRAVEL_TO_STATION
    ;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.INFO;
    }
}
