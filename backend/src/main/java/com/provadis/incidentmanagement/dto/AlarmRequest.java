package com.provadis.incidentmanagement.dto;

import com.provadis.incidentmanagement.model.AlarmSeverity;

public record AlarmRequest(String source, String message, AlarmSeverity severity) {
}
