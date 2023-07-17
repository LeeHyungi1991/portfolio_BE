package com.portfolio.models.repository.location;

import com.portfolio.models.entity.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository {
    public Location test(long seq);
    List<Location> findAllOrderByXYDesc(Double x, Double y, String keyword, Pageable pageRequest);
    Optional<Location> findByXY(Double xAxis, Double yAxis);
    Optional<Location> findByClosestXY(double lat, double lon);
}
