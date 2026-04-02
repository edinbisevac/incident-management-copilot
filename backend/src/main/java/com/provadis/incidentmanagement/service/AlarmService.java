package com.provadis.incidentmanagement.service;

import com.provadis.incidentmanagement.dto.AlarmRequest;
import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.model.AlarmSeverity;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.model.IncidentStatus;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmService {

    private static final List<IncidentStatus> CLOSED_STATUSES = List.of(IncidentStatus.RESOLVED, IncidentStatus.CLOSED);

    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;

    public AlarmService(AlarmRepository alarmRepository, IncidentRepository incidentRepository) {
        this.alarmRepository = alarmRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public Alarm createAlarm(AlarmRequest request) {
        String source = requireText(request.source(), "source");
        String message = requireText(request.message(), "message");
        AlarmSeverity severity = request.severity() == null ? AlarmSeverity.INFO : request.severity();

        Alarm alarm = new Alarm(
                source,
                message,
            severity,
                LocalDateTime.now()
        );
        Alarm savedAlarm = alarmRepository.save(alarm);

        IncidentPriority mappedPriority = IncidentPriority.fromSeverity(savedAlarm.getSeverity());
        Incident incident = incidentRepository.findFirstBySourceAndStatusNotInOrderByIdAsc(source, CLOSED_STATUSES)
                .orElseGet(() -> new Incident(
                        "Incident for " + source,
                        message,
                        IncidentStatus.OPEN,
                        mappedPriority,
                        source
                ));

        incident.setDescription(message);
        incident.setPriority(IncidentPriority.higherOf(incident.getPriority(), mappedPriority));
        if (incident.getTitle() == null || incident.getTitle().isBlank()) {
            incident.setTitle("Incident for " + source);
        }
        if (incident.getSource() == null || incident.getSource().isBlank()) {
            incident.setSource(source);
        }

        incidentRepository.save(incident);
        return savedAlarm;
    }

    public List<Alarm> getAlarms(String source) {
        String normalizedSource = normalizeOptionalText(source);
        if (normalizedSource == null) {
            return alarmRepository.findAllByOrderByCreatedAtDesc();
        }

        return alarmRepository.findAllBySourceOrderByCreatedAtDesc(normalizedSource);
    }

    public void deleteAlarm(Long id) {
        if (!alarmRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alarm not found");
        }

        alarmRepository.deleteById(id);
    }

    public void deleteAllAlarms() {
        alarmRepository.deleteAll();
    }

    private String requireText(String value, String fieldName) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
