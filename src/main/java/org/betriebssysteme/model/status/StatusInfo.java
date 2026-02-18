package org.betriebssysteme.model.status;

/**
 * Enum representing informational status types.
 */
public enum StatusInfo implements Status {
    HAND_OVER_PRODUCT,
    COLLECT_CARGO,
    DELIVER_CARGO,
    TRANSPORT_CARGO,
    TRAVEL_TO_HEADQUARTERS,
    OPERATIONAL,
    TRAVEL_TO_STATION
    ;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.INFO;
    }
}
