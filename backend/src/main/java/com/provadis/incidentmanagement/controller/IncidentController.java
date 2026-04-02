package com.provadis.incidentmanagement.controller;

import com.provadis.incidentmanagement.dto.IncidentPriorityUpdateRequest;
import com.provadis.incidentmanagement.dto.IncidentRequest;
import com.provadis.incidentmanagement.dto.IncidentStatusUpdateRequest;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<Incident> getIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Incident getIncident(@PathVariable Long id) {
        return incidentService.getIncident(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident createIncident(@RequestBody IncidentRequest request) {
        return incidentService.createIncident(request);
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @RequestBody IncidentStatusUpdateRequest request) {
        return incidentService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/priority")
    public Incident updatePriority(@PathVariable Long id, @RequestBody IncidentPriorityUpdateRequest request) {
        return incidentService.updatePriority(id, request.priority());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }
}
