package org.betriebssysteme.model.status;

public interface Status {
    StatusTyp getStatusTyp();

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
