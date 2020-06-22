package com.vipassistant.web.backend.repository;

import com.vipassistant.web.backend.entity.Beacon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeaconRepository extends JpaRepository<Beacon, Long> {
}
