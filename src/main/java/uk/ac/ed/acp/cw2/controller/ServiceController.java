package uk.ac.ed.acp.cw2.controller;

import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.net.URL;
import java.time.Instant;

import static org.aspectj.runtime.internal.Conversions.doubleValue;
import static uk.ac.ed.acp.cw2.controller.Util.*;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    private final WebClient webClient = WebClient.create("http://localhost:8080");

    @Value("${ilp.service.url}")
    public URL serviceUrl;


    @GetMapping("/api/v1/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }


    @GetMapping("actuator/health")
    public String health() {
        // no need to validat einput because there is no input
        // call a function that call the api
        return webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .block(); // block() because this is a regular controller
    }

    @GetMapping("/api/v1/uid")
    public String uid() {
        return "s12345678";
    }

    @PostMapping("/api/v1/distanceTo")
    public double distanceTo(@RequestBody Map<String, Object> positions) {
        validatePositions(positions);

        // extract positions
        Map<String, Object> position2 = (Map<String, Object>) positions.get("position2");
        double position2lng = ((Number) position2.get("lng")).doubleValue();
        double position2lat = ((Number) position2.get("lat")).doubleValue();

        Map<String, Object> position1 = (Map<String, Object>) positions.get("position1");
        double position1lng = ((Number) position1.get("lng")).doubleValue();
        double position1lat = ((Number) position1.get("lat")).doubleValue();

        // calculate distance
        double distance = euclideanDistance(position1lng, position1lat, position2lng, position2lat);

        return distance;
    }

    @PostMapping("/api/v1/isCloseTo")
    public boolean isCloseTo(@RequestBody Map<String, Object> positions) throws ResponseStatusException {
        validatePositions(positions);

        double distance = distanceTo(positions);
        if (distance < 0.00015) {
            return true;
        }
        return false;
    }

    @PostMapping("/api/v1/nextPosition")
    public String nextPosition(@RequestBody Map<String, Object> positionAndAngle) throws ResponseStatusException {
        // input validation
        if (!positionAndAngle.containsKey("start") || !positionAndAngle.containsKey("angle")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Either start or angle are missing from the input"
            );
        }
        validateOnePosition((Map<String, Object>) positionAndAngle.get("start"));

        // process input to prepare parameters for calculating
        Map<String, Object> lnglat = (Map<String, Object>) positionAndAngle.get("start");
        Map<String, Object> angle = (Map<String, Object>) positionAndAngle.get("angle");
        double lng = Math.toRadians(((Number) lnglat.get("lng")).doubleValue());
        double lat = Math.toRadians(((Number) lnglat.get("lat")).doubleValue());
        double bearing = Math.toRadians(((Number) angle.get("angle")).doubleValue());


        return moveTo(lat, lng, bearing);
    }

    @PostMapping("/api/v1/isInRegion")
    public boolean isInRegion(@RequestBody Map<String, Object> positionAndRegion) throws ResponseStatusException {
        // input validation
        if (positionAndRegion == null || positionAndRegion.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: input cannot be null or empty"
            );
        }
        Map<String, Object> position = (Map<String, Object>) positionAndRegion.get("position");
        validateOnePosition(position);
        Map<String, Object> region = (Map<String, Object>) positionAndRegion.get("region");
        if  (!region.containsKey("name") || !region.containsKey("vertices")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: input has missing name or vertices"
            );
        }
        List<Map<String, Object>> vertices = (List<Map<String, Object>>) region.get("vertices");
        validateVertices(vertices);
        if (!(region.get("name") instanceof String)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error: name should be a string"
            );
        };

        // prepare inputs for teh isPointInPolygon function
        double[][] verticesMatrix = verticesToArray(vertices);
        double positionLng = ((Number) position.get("lng")).doubleValue();
        double positionLat = ((Number) position.get("lat")).doubleValue();
        return isPointInPolygon(positionLng, positionLat,  verticesMatrix);
    }


}

