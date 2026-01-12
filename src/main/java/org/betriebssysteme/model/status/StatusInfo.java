package org.betriebssysteme.model.status;

public enum StatusInfo implements Status {
    PRODUCING,
    STORE_PRODUCT,
    HAND_OVER_PRODUCT;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.INFO;
    }
}
