package org.betriebssysteme.model.status;

public enum StatusWarning implements Status{
    STOPPED,
    FULL,
    EMPTY,
    ERROR;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.WARNING;
    }
}
