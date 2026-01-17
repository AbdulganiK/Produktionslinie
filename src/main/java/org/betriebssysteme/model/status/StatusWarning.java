package org.betriebssysteme.model.status;

/**
 * Enum representing warning status types.
 */
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
