package fpt.project.NeoNHS.service;

import java.util.List;

public interface GeoService {
    void syncCheckinsToRedis();

    void addCheckinToRedis(String checkinId, double longitude, double latitude);
    void removeCheckinFromRedis(String checkinId);
    void updateCheckinInRedis(String checkinId, double longitude, double latitude);
    List<String> getCheckinsInRadius(double latitude, double longitude, double radiusMeters);
    double calculateDistanceManually(double lat1, double lon1, double lat2, double lon2);
}
