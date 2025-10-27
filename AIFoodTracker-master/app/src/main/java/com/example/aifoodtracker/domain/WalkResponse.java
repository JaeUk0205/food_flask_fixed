package com.example.aifoodtracker.domain;

import java.util.List;

public class WalkResponse {
    private double targetDistance;
    private List<WalkRoute> nearbyRoutes;
    private List<WalkRoute> famousRoutes;

    public double getTargetDistance() { return targetDistance; }
    public List<WalkRoute> getNearbyRoutes() { return nearbyRoutes; }
    public List<WalkRoute> getFamousRoutes() { return famousRoutes; }
}