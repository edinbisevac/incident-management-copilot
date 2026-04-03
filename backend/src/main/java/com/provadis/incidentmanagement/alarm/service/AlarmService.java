package com.provadis.incidentmanagement.alarm.service;

import com.provadis.incidentmanagement.alarm.exception.AlarmNotFoundException;
import com.provadis.incidentmanagement.alarm.model.Alarm;
import com.provadis.incidentmanagement.alarm.model.AlarmSeverity;
import com.provadis.incidentmanagement.alarm.repository.AlarmRepository;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;
import com.provadis.incidentmanagement.incident.repository.IncidentRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final EnumSet<IncidentStatus> OPEN_INCIDENT_STATUSES =
            EnumSet.of(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS);

    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;

    public List<Alarm> findAll(String source) {
        if (StringUtils.hasText(source)) {
            return alarmRepository.findBySourceOrderByCreatedAtDesc(source.trim());
        }

        return alarmRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Alarm create(String source, String message, AlarmSeverity severity, LocalDateTime createdAt) {
        Incident incident = incidentRepository
                .findFirstBySourceAndStatusInOrderByIdAsc(source, OPEN_INCIDENT_STATUSES)
                .map(existingIncident -> updateIncidentFromAlarm(existingIncident, message, severity))
                .orElseGet(() -> createIncidentFromAlarm(source, message, severity));

        Alarm alarm = Alarm.builder()
                .source(source)
                .message(message)
                .severity(severity)
                .createdAt(createdAt)
                .incident(incident)
                .build();

        return alarmRepository.save(alarm);
    }

    public void delete(Long id) {
        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new AlarmNotFoundException(id));
        alarmRepository.delete(alarm);
    }

    private Incident createIncidentFromAlarm(String source, String message, AlarmSeverity severity) {
        Incident incident = Incident.builder()
                .title("Incident for source " + source)
                .description(message)
                .status(IncidentStatus.OPEN)
                .priority(mapSeverityToPriority(severity))
                .source(source)
                .build();

        return incidentRepository.save(incident);
    }

    private Incident updateIncidentFromAlarm(Incident incident, String message, AlarmSeverity severity) {
        incident.setDescription(message);
        incident.setPriority(getHighestPriority(incident, severity));
        return incidentRepository.save(incident);
    }

    private IncidentPriority getHighestPriority(Incident incident, AlarmSeverity newSeverity) {
        IncidentPriority highestPriority = mapSeverityToPriority(newSeverity);

        for (Alarm existingAlarm : alarmRepository.findByIncident(incident)) {
            IncidentPriority mappedPriority = mapSeverityToPriority(existingAlarm.getSeverity());
            if (mappedPriority.ordinal() > highestPriority.ordinal()) {
                highestPriority = mappedPriority;
            }
        }

        if (incident.getPriority() != null && incident.getPriority().ordinal() > highestPriority.ordinal()) {
            return incident.getPriority();
        }

        return highestPriority;
    }

    private IncidentPriority mapSeverityToPriority(AlarmSeverity severity) {
        return switch (severity) {
            case CRITICAL -> IncidentPriority.HIGH;
            case MAJOR -> IncidentPriority.MEDIUM;
            case MINOR -> IncidentPriority.LOW;
        };
    }
}