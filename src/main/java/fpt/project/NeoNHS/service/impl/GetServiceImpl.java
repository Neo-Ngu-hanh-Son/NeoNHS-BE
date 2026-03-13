package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.GeoConstants;
import fpt.project.NeoNHS.entity.CheckinPoint;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.service.GeoService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class GetServiceImpl implements GeoService {
    private final RedisTemplate<String, String> redisTemplate;
    private final CheckinPointRepository repository;

    // Run this to sync with redis whenever you CRUD or when the server is up and running
    public void syncCheckinsToRedis() {
        List<CheckinPoint> points = repository.findAll();

        Map<String, Point> memberCoordsMap = new HashMap<>();
        for (CheckinPoint p : points) {
            memberCoordsMap.put(p.getId().toString(), new Point(p.getLongitude().doubleValue(),
                    p.getLatitude().doubleValue()));
        }
        redisTemplate.opsForGeo().add(GeoConstants.GEO_KEY, memberCoordsMap);
    }

    @Override
    public void addCheckinToRedis(String checkinId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(GeoConstants.GEO_KEY, new Point(longitude, latitude), checkinId);
    }

    @Override
    public void removeCheckinFromRedis(String checkinId) {
        redisTemplate.opsForGeo().remove(GeoConstants.GEO_KEY, checkinId);
    }

    @Override
    public void updateCheckinInRedis(String checkinId, double longitude, double latitude) {
        // Update == Add
        addCheckinToRedis(checkinId, longitude, latitude);
    }

    // Long, lat NOT lat, long
    public List<String> getCheckinsInRadius(double latitude, double longitude, double radiusMeters) {
        Circle circle = new Circle(new Point(longitude, latitude), new Distance(radiusMeters, Metrics.METERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(GeoConstants.GEO_KEY, circle);

        List<String> idList =  results.getContent().stream()
                .map(res -> res.getContent().getName()) // Returns the ID of the points
                .toList();
        return idList;
    }

    public double calculateDistanceManually(double lat1, double lon1, double lat2, double lon2) {
        return haversineDistance(lat1, lon1, lat2, lon2);
    }

    // Extracted helper method for the math
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // Earth's radius in meters

        // Convert decimal degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

}
