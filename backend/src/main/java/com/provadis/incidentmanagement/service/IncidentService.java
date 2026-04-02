package com.provadis.incidentmanagement.service;

import com.provadis.incidentmanagement.dto.IncidentRequest;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.model.IncidentStatus;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AlarmRepository alarmRepository;

    public IncidentService(IncidentRepository incidentRepository, AlarmRepository alarmRepository) {
        this.incidentRepository = incidentRepository;
        this.alarmRepository = alarmRepository;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAllByOrderByIdDesc();
    }

    public Incident getIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }

    public Incident createIncident(IncidentRequest request) {
        String title = requireText(request.title(), "title");
        String description = requireText(request.description(), "description");
        String source = normalizeOptionalText(request.source());

        IncidentPriority requestedPriority = request.priority() == null ? IncidentPriority.MEDIUM : request.priority();
        IncidentPriority effectivePriority = source == null
                ? requestedPriority
                : IncidentPriority.higherOf(highestPriorityFromSource(source), requestedPriority);

        Incident incident = new Incident(
                title,
                description,
                request.status() == null ? IncidentStatus.OPEN : request.status(),
                effectivePriority,
                source
        );

        return incidentRepository.save(incident);
    }

    public Incident updateStatus(Long id, IncidentStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        Incident incident = getIncident(id);
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    public Incident updatePriority(Long id, IncidentPriority priority) {
        if (priority == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority is required");
        }

        Incident incident = getIncident(id);
        IncidentPriority effectivePriority = incident.getSource() == null
                ? priority
                : IncidentPriority.higherOf(highestPriorityFromSource(incident.getSource()), priority);

        incident.setPriority(effectivePriority);
        return incidentRepository.save(incident);
    }

    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found");
        }

        incidentRepository.deleteById(id);
    }

    private IncidentPriority highestPriorityFromSource(String source) {
        return alarmRepository.findAllBySourceOrderByCreatedAtDesc(source).stream()
                .map(alarm -> IncidentPriority.fromSeverity(alarm.getSeverity()))
                .reduce(IncidentPriority::higherOf)
                .orElse(IncidentPriority.MEDIUM);
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
