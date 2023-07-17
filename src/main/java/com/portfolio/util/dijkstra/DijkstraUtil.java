package com.portfolio.util.dijkstra;

import com.portfolio.models.entity.Line;
import com.portfolio.models.entity.Location;

import java.util.*;

public class DijkstraUtil {
    public static List<Location> getShortestPath(Location start, Location end) {
        Map<Location, Location> prevs = new HashMap<>();
        Candidate from = new Candidate(start, 0D);
        PriorityQueue<Candidate> candidates = new PriorityQueue<>();
        HashMap<Location, Double> minDists = new HashMap<>();
        candidates.add(from);
        prevs.put(start, null);
        while (!candidates.isEmpty()) {
            Candidate current = candidates.poll();
            Location location = current.getCurrentLocation();
            if (location.getSeq().equals(end.getSeq())) {
                break;
            }
            for (Line line : current.getCurrentLocation().getLinesOne()) {
                Location nextLocation = line.getTwo();
                goToTheNext(prevs, candidates, minDists, current, location, line, nextLocation);
            }
            for (Line line : current.getCurrentLocation().getLinesTwo()) {
                Location nextLocation = line.getOne();
                goToTheNext(prevs, candidates, minDists, current, location, line, nextLocation);
            }
        }
        LinkedList<Location> path = new LinkedList<>();

        Location location = end;
        while (location != null) {
            path.addFirst(location);
            location = prevs.get(location);
        }
        return path;
    }

    private static void goToTheNext(Map<Location, Location> prevs, PriorityQueue<Candidate> candidates, HashMap<Location, Double> minDists, Candidate current, Location location, Line line, Location nextLocation) {
        double weight = line.getDistance();
        double newDist = current.getDistance() + weight;
        double nextMinDist = minDists.getOrDefault(nextLocation, Double.MAX_VALUE);
        if (newDist >= nextMinDist) {
            return;
        }
        minDists.put(nextLocation, newDist);
        prevs.put(nextLocation, location);
        candidates.add(new Candidate(nextLocation, newDist));
    }
}
