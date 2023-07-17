package com.portfolio.models.repository.location;

import com.portfolio.models.entity.Location;
import com.portfolio.models.repository.CommonRepositoryImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class LocationRepositoryImpl extends CommonRepositoryImpl<Location, Long> implements LocationRepository {
    @Override
    public Location test(long seq) {
        return createTypedQuery("select l from Location l where l.seq = :seq", Location.class)
                .setParameter("seq", seq)
                .getSingleResult();
    }

    @Override
    public List<Location> findAllOrderByXYDesc(Double x, Double y, String keyword, Pageable pageRequest) {
        if (keyword == null || keyword.isEmpty()) {
            keyword = "";
        }
        StringBuilder jpql = new StringBuilder();
        jpql.append("select l from Location l\n");
        jpql.append("order by\n");
        jpql.append("(case\n");
        jpql.append("when l.name like :keyword then 1\n");
        jpql.append("else 2\n");
        jpql.append("end),\n");
        jpql.append("(ABS(:x - l.xAxis) + ABS(:y - l.yAxis)) asc");
        return createTypedQuery(jpql.toString(), Location.class)
                .setParameter("x", x)
                .setParameter("y", y)
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();
//        return createTypedQuery(jpql, Location.class)
//                .setParameter("keyword", "%" + keyword + "%")
//                .setParameter("x", x)
//                .setParameter("y", y)
//                .getResultList();
    }

    @Override
    public Optional<Location> findByXY(Double xAxis, Double yAxis) {
        return createTypedQuery("select l from Location l where l.xAxis = :xAxis and l.yAxis = :yAxis", Location.class)
                .setParameter("xAxis", xAxis)
                .setParameter("yAxis", yAxis)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Location> findByClosestXY(double lat, double lon) {
        return createTypedQuery("select l from Location l order by (ABS(:lat - l.xAxis) + ABS(:lon - l.yAxis)) asc", Location.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .getResultList()
                .stream()
                .findFirst();
    }
}
