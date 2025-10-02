package uk.ac.ed.acp.cw2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.DistanceToRequest;
import uk.ac.ed.acp.cw2.dto.DistanceToResponse;
import uk.ac.ed.acp.cw2.dto.IsCloseToRequest;
import uk.ac.ed.acp.cw2.dto.IsCloseToResponse;
import uk.ac.ed.acp.cw2.model.Position;
import uk.ac.ed.acp.cw2.service.DistanceService;

import java.net.URL;

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

        return ResponseEntity.ok(new DistanceToResponse(distance));
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
        return ResponseEntity.ok(new IsCloseToResponse(isClose));
    }

    @PostMapping("/nextPosition")
    public String nextPosition(){
        return "Blank";
    }

    @PostMapping("/isInRegion")
    public String isInRegion(){
        return "Blank";
    }


}
