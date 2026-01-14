package org.betriebssysteme.model.status;

public interface Status {
    /**
     * Get the type of the status.
     *
     * @return The StatusTyp of the status.
     */
    StatusTyp getStatusTyp();

    /**
     * Cast a string to the corresponding Status enum value.
     *
     * @param status The string representation of the status.
     * @return The corresponding Status enum value, or null if not found.
     */
    static Status castToStatus(String status)
    {
        for (StatusInfo statusInfo : StatusInfo.values()) {
            if (statusInfo.name().equals(status)) {
                return statusInfo;
            }
        }
        for (StatusCritical statusCritical : StatusCritical.values()) {
            if (statusCritical.name().equals(status)) {
                return statusCritical;
            }
        }
        for (StatusWarning statusWarning : StatusWarning.values()) {
            if (statusWarning.name().equals(status)) {
                return statusWarning;
            }
        }
        return null;
    }
}
