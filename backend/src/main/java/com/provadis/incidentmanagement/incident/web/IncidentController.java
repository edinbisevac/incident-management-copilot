package com.provadis.incidentmanagement.incident.web;

import com.provadis.incidentmanagement.incident.exception.IncidentNotFoundException;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.service.IncidentService;
import com.provadis.incidentmanagement.incident.web.dto.CreateIncidentRequest;
import com.provadis.incidentmanagement.incident.web.dto.UpdateIncidentPriorityRequest;
import com.provadis.incidentmanagement.incident.web.dto.UpdateIncidentStatusRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentService.findAll();
    }

    @GetMapping("/{id}")
    public Incident getIncidentById(@PathVariable Long id) {
        return incidentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident createIncident(@RequestBody CreateIncidentRequest request) {
        validateCreateRequest(request);

        Incident incident = Incident.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .status(request.status())
                .priority(request.priority())
                .source(request.source().trim())
                .build();

        return incidentService.create(incident);
    }

    @PatchMapping("/{id}/status")
    public Incident updateIncidentStatus(@PathVariable Long id, @RequestBody UpdateIncidentStatusRequest request) {
        if (request == null || request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required.");
        }

        return incidentService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/priority")
    public Incident updateIncidentPriority(@PathVariable Long id, @RequestBody UpdateIncidentPriorityRequest request) {
        if (request == null || request.priority() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority is required.");
        }

        return incidentService.updatePriority(id, request.priority());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<String> handleIncidentNotFound(IncidentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    private void validateCreateRequest(CreateIncidentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        if (!StringUtils.hasText(request.title())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required.");
        }

        if (!StringUtils.hasText(request.description())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description is required.");
        }

        if (!StringUtils.hasText(request.source())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source is required.");
        }

        if (request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required.");
        }

        if (request.priority() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority is required.");
        }
    }
}