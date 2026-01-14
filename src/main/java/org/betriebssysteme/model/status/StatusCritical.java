package org.betriebssysteme.model.status;

/**
 * Enum representing critical status types.
 */
public enum StatusCritical implements Status{
    LOW_CAPACITY;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.CRITICAL;
    }
}
