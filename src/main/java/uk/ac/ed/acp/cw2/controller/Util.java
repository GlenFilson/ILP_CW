package uk.ac.ed.acp.cw2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.net.URISyntaxException;
import java.util.Map;

public class Util {
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static String moveTo(double lat, double lng, double bearing) {
        // Pure function: inputs -> JSON string output, no side effects.

        // default step in meters (assumption)
        final double R = 6371000.0; // Earth radius in meters
        final double distance = 1.0;
        double angularDistance = distance / R;

        // destination formula (spherical Earth)
        double newLatRad = Math.asin(
                Math.sin(lat) * Math.cos(angularDistance) +
                        Math.cos(lat) * Math.sin(angularDistance) * Math.cos(bearing)
        );

        double newLngRad = lng + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat),
                Math.cos(angularDistance) - Math.sin(lat) * Math.sin(newLatRad)
        );

        // normalize longitude to [-PI, PI]
        newLngRad = (newLngRad + Math.PI * 3) % (Math.PI * 2) - Math.PI;

        double newLat = Math.toDegrees(newLatRad);
        double newLng = Math.toDegrees(newLngRad);

        // Return JSON with requested key order: {"lng": ..., "lat": ...}
        return String.format("{\"lng\": %.12f, \"lat\": %.12f}", newLng, newLat);
    }

    public static void validateOnePosition(Map<String, Object> position) throws ResponseStatusException{
        if (!position.containsKey("lng") || !position.containsKey("lat")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: Either lng or lat are missing from the input"
            );
        }

        Object lngObj = position.get("lng");
        Object latObj = position.get("lat");

        if (!(lngObj instanceof Number) || !(latObj instanceof Number)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: lng and lat must be numeric"
            );
        }

    }

    public static void validatePositions(Map<String, Object> positions) throws ResponseStatusException {
        if (positions == null || positions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: input cannot be null or empty"
            );
        }
        if ((!positions.containsKey("position2")) || (!positions.containsKey("position1"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: either position1 or position2 is missing"
            );
        }
        validateOnePosition((Map<String, Object>) positions.get("position2"));
        validateOnePosition((Map<String, Object>) positions.get("position1"));

    }


}
