package com.provadis.incidentmanagement.incident.exception;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(Long incidentId) {
        super("Incident with id " + incidentId + " was not found.");
    }
}