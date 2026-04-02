package com.provadis.incidentmanagement.controller;

import com.provadis.incidentmanagement.dto.AlarmRequest;
import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.service.AlarmService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm createAlarm(@RequestBody AlarmRequest request) {
        return alarmService.createAlarm(request);
    }

    @GetMapping
    public List<Alarm> getAlarms(@RequestParam(required = false) String source) {
        return alarmService.getAlarms(source);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlarm(@PathVariable Long id) {
        alarmService.deleteAlarm(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllAlarms() {
        alarmService.deleteAllAlarms();
    }
}
