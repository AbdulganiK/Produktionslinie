package org.betriebssysteme.model.status;

/**
 * The Status interface represents a general status type that can be either a StatusInfo, StatusCritical, or StatusWarning.
 * It provides a method to retrieve the StatusTyp, which indicates the type of the status.
 */
public interface Status {
    /**
     * Get the type of the status.
     *
     * @return The StatusTyp of the status.
     */
    StatusTyp getStatusTyp();
}
