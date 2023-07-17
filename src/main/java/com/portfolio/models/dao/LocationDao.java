package com.portfolio.models.dao;

import com.portfolio.models.entity.Location;
import com.portfolio.models.repository.location.LocationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationDao extends LocationRepository, JpaRepository<Location, Long> {
}
