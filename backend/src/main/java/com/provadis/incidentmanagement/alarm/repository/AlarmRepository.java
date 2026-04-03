package com.provadis.incidentmanagement.alarm.repository;

import com.provadis.incidentmanagement.alarm.model.Alarm;
import com.provadis.incidentmanagement.incident.model.Incident;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

	List<Alarm> findAllByOrderByCreatedAtDesc();

	List<Alarm> findBySourceOrderByCreatedAtDesc(String source);

	List<Alarm> findByIncident(Incident incident);

	void deleteByIncident(Incident incident);
}