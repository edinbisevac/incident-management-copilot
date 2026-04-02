package com.provadis.incidentmanagement.repository;

import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByIdDesc();

    Optional<Incident> findFirstBySourceAndStatusNotInOrderByIdAsc(String source, Collection<IncidentStatus> statuses);
}
