package org.betriebssysteme.model.status;

public enum StatusCritical implements Status{
    LOW_CAPACITY;

    @Override
    public StatusTyp getStatusTyp() {
        return StatusTyp.CRITICAL;
    }
}
