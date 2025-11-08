package uk.ac.ed.acp.cw1.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.dto.Region;
import uk.ac.ed.acp.cw1.service.DistanceService;
import uk.ac.ed.acp.cw1.service.DroneService;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final DroneService droneService;

    public ServiceController(DistanceService distanceService, DroneService droneService){
        this.distanceService = distanceService;
        this.droneService = droneService;
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
    public ResponseEntity<?> distanceTo(@RequestBody @Valid DistanceToRequest request){

        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();
        Double distance = distanceService.euclideanDistance(position1, position2);

        return ResponseEntity.ok(distance);
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo(@RequestBody @Valid IsCloseToRequest request){

        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();

        return ResponseEntity.ok(distanceService.isCloseTo(position1, position2));
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition(@RequestBody @Valid NextPositionRequest request){

        Position position = request.getStart();
        Double angle = request.getAngle();
        Position nextPosition = distanceService.nextPosition(position, angle);

        return ResponseEntity.ok(new NextPositionResponse(nextPosition.getLat(),  nextPosition.getLng()));
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<?> isInRegion(@RequestBody @Valid IsInRegionRequest request){

        Region region = request.getRegion();

        return ResponseEntity.ok(distanceService.isInRegion(request.getPosition(), region));
    }


    @GetMapping("dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable boolean state){
        return ResponseEntity.ok(droneService.getDronesWithCooling(state));
    }

    @GetMapping("droneDetails/{id}")
    public ResponseEntity<?> droneDetails(@PathVariable Integer id){
        Drone drone = droneService.getDroneById(id);
        if (drone == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(drone);
    }

    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attributeName,
                                                    @PathVariable String attributeValue){
        return ResponseEntity.ok(droneService.queryAsPath(attributeName, attributeValue));
    }

    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@RequestBody List<QueryAttribute> attributes) {
        return ResponseEntity.ok(droneService.query(attributes));
    }






    /**
     * handles bad requests, mapping each validation error and logging it
     * logs the field name causing the error with its error message
     * @param ex the exception being handled
     */
    //handles bad requests - status code 400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationExceptions(MethodArgumentNotValidException ex){
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            logger.warn(fieldName + " : " + errorMessage);
        });
    }


}
