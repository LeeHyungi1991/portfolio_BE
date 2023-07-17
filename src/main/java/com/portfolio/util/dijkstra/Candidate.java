package com.portfolio.util.dijkstra;

import com.portfolio.models.entity.Location;
import lombok.Data;

import java.util.Comparator;

@Data
public class Candidate implements Comparator<Candidate> {
    public Location currentLocation;
    public Double distance;

    public Candidate(Location currentLocation, Double distance) {
        this.currentLocation = currentLocation;
        this.distance = distance;
    }

    @Override
    public int compare(Candidate o1, Candidate o2) {
        return o1.getDistance().compareTo(o2.getDistance());
    }
}
