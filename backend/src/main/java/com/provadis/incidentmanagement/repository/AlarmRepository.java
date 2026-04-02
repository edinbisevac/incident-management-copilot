package com.provadis.incidentmanagement.repository;

import com.provadis.incidentmanagement.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findAllByOrderByCreatedAtDesc();

    List<Alarm> findAllBySourceOrderByCreatedAtDesc(String source);
}
