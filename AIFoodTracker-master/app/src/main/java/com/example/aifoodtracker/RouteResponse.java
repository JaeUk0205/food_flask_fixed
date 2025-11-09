package com.example.aifoodtracker;

public class RouteResponse {
    public String polyline;     // 인코딩된 경로
    public Integer distance_m;  // 거리 (미터)
    public Integer duration_s;  // 시간 (초)
    public String mode;         // 모드 (walking, driving 등)
}
