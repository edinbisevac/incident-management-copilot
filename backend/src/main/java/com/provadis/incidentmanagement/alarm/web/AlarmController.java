package com.provadis.incidentmanagement.alarm.web;

import com.provadis.incidentmanagement.alarm.exception.AlarmNotFoundException;
import com.provadis.incidentmanagement.alarm.model.Alarm;
import com.provadis.incidentmanagement.alarm.service.AlarmService;
import com.provadis.incidentmanagement.alarm.web.dto.CreateAlarmRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm createAlarm(@RequestBody CreateAlarmRequest request) {
        validateCreateRequest(request);

        return alarmService.create(
                request.source().trim(),
                request.message().trim(),
                request.severity(),
                request.createdAt() != null ? request.createdAt() : LocalDateTime.now()
        );
    }

    @GetMapping
    public List<Alarm> getAlarms(@RequestParam(required = false) String source) {
        return alarmService.findAll(source);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlarm(@PathVariable Long id) {
        alarmService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AlarmNotFoundException.class)
    public ResponseEntity<String> handleAlarmNotFound(AlarmNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    private void validateCreateRequest(CreateAlarmRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        if (!StringUtils.hasText(request.source())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source is required.");
        }

        if (!StringUtils.hasText(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required.");
        }

        if (request.severity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "severity is required.");
        }
    }
}