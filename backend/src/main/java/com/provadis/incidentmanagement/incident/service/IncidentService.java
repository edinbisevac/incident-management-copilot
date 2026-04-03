package com.provadis.incidentmanagement.incident.service;

import com.provadis.incidentmanagement.incident.exception.IncidentNotFoundException;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;
import com.provadis.incidentmanagement.incident.repository.IncidentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }

    public Incident findById(Long id) {
        return getIncidentOrThrow(id);
    }

    public Incident create(Incident incident) {
        incident.setId(null);
        return incidentRepository.save(incident);
    }

    public Incident updateStatus(Long id, IncidentStatus status) {
        Incident incident = getIncidentOrThrow(id);
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    public Incident updatePriority(Long id, IncidentPriority priority) {
        Incident incident = getIncidentOrThrow(id);
        incident.setPriority(priority);
        return incidentRepository.save(incident);
    }

    public void delete(Long id) {
        Incident incident = getIncidentOrThrow(id);
        incidentRepository.delete(incident);
    }

    private Incident getIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }
}