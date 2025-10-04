package uk.ac.ed.acp.cw2.controller;

import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.model.Position;
import uk.ac.ed.acp.cw2.model.Region;
import uk.ac.ed.acp.cw2.service.DistanceService;

import java.net.URL;
import java.util.List;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    //service injection
    private final DistanceService distanceService;
    public ServiceController(DistanceService distanceService){
        this.distanceService = distanceService;
    }
    @Value("${ilp.service.url}")
    public URL serviceUrl;


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2539057";
    }


    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@RequestBody DistanceToRequest request){
        if (request == null || request.getPosition1() == null || request.getPosition2() == null
                || request.getPosition1().getLat() == null || request.getPosition1().getLng() == null
                || request.getPosition2().getLat() == null || request.getPosition2().getLng() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request");
        }

        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();

        Double distance = distanceService.euclideanDistance(position1, position2);

        return ResponseEntity.ok(distance);
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo(@RequestBody IsCloseToRequest request){
        if (request == null || request.getPosition1() == null || request.getPosition2() == null
        || request.getPosition1().getLat() == null || request.getPosition1().getLng() == null
        || request.getPosition2().getLat() == null || request.getPosition2().getLng() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request");
        }

        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();
        Boolean isClose = distanceService.isCloseTo(position1, position2);

        return ResponseEntity.ok(isClose);
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition(@RequestBody NextPositionRequest request){
        //checks for null body
        if (request == null || request.getStart() == null
                || request.getStart().getLat() == null
                || request.getStart().getLng() == null
                ||request.getAngle() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request");
        }
        Position position = request.getStart();
        Double angle = request.getAngle();
        Position nextPosition = distanceService.nextPosition(position, angle);

        if (nextPosition == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request");
        NextPositionResponse response = new  NextPositionResponse();
        response.setLat(nextPosition.getLat());
        response.setLng(nextPosition.getLng());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<?> isInRegion(@RequestBody IsInRegionRequest request){
        //check if any parts of response are empty
        if (request == null || request.getPosition() == null
        ||request.getPosition().getLat() == null || request.getPosition().getLng() == null
        || request.getRegion() == null || request.getRegion().getName() == null || request.getRegion().getVertices() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request");
        }

        Region region = request.getRegion();
        List<Position> vertices = request.getRegion().getVertices();
        //check there are at least 4 points as per the coursework spec, "A region is usually rectangular yet can be any polygon"
        //needs to be 4 points as first and last point should be duplicate to close the polygon. aka there are only 3 distinct points
        if (vertices.size() < 4){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request. Not enough vertices");
        }

        //check all vertices have lng and lat, are valid positions
        for (Position vertex : vertices) {
            if (vertex.getLng() == null || vertex.getLat() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request. Not all vertices are formatted correctly");
            }
        }

        //check first and last vertices are the same, the polygon is closed
        if (!vertices.getFirst().getLng().equals(vertices.getLast().getLng())
        || !vertices.getFirst().getLat().equals(vertices.getLast().getLat())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request. Polygon is not closed, first and last vertices arent equal.");
        }


        return ResponseEntity.ok(distanceService.isInRegion(request.getPosition(), region));
    }


}
