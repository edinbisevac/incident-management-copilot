package com.provadis.incidentmanagement;

import com.provadis.incidentmanagement.dto.AlarmRequest;
import com.provadis.incidentmanagement.model.AlarmSeverity;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import com.provadis.incidentmanagement.service.AlarmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AlarmServiceIntegrationTests {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    @Test
    void createsSingleOpenIncidentPerSourceAndKeepsHighestPriority() {
        alarmService.createAlarm(new AlarmRequest("router-01", "CPU load high", AlarmSeverity.MAJOR));
        alarmService.createAlarm(new AlarmRequest("router-01", "Packet loss detected", AlarmSeverity.CRITICAL));
        alarmService.createAlarm(new AlarmRequest("router-01", "Minor jitter detected", AlarmSeverity.MINOR));

        List<Incident> incidents = incidentRepository.findAll();

        assertThat(incidentRepository.findAll()).hasSize(1);
        assertThat(alarmRepository.findAll()).hasSize(3);
        assertThat(incidents.get(0).getDescription()).isEqualTo("Minor jitter detected");
        assertThat(incidents.get(0).getPriority()).isEqualTo(IncidentPriority.HIGH);
        assertThat(incidents.get(0).getSource()).isEqualTo("router-01");
    }
}