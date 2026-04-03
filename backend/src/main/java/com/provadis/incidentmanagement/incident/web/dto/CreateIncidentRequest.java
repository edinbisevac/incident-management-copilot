package com.provadis.incidentmanagement.incident.web.dto;

import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;

public record CreateIncidentRequest(
        String title,
        String description,
        IncidentStatus status,
        IncidentPriority priority,
        String source
) {
}