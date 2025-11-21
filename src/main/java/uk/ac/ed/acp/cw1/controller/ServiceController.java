package uk.ac.ed.acp.cw1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.dto.Region;
import uk.ac.ed.acp.cw1.service.DistanceService;
import uk.ac.ed.acp.cw1.service.DroneService;
import uk.ac.ed.acp.cw1.service.ExternalAPIService;
import uk.ac.ed.acp.cw1.service.PathfindingService;

import java.net.URL;
import java.util.*;

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
    private final ExternalAPIService externalAPIService;
    private final PathfindingService pathfindingService;

    public ServiceController(DistanceService distanceService, DroneService droneService, ExternalAPIService externalAPIService, PathfindingService pathfindingService) {
        this.distanceService = distanceService;
        this.droneService = droneService;
        this.externalAPIService = externalAPIService;
        this.pathfindingService = pathfindingService;
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
    public ResponseEntity<?> droneDetails(@PathVariable String id){
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

    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(@RequestBody List<MedDispatchRec> dispatches) {
        return ResponseEntity.ok(droneService.queryAvailableDrones(dispatches));
    }


    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<?> calcDeliveryPath(@RequestBody List<MedDispatchRec> dispatches){
        return ResponseEntity.ok(pathfindingService.calcDeliveryPath(dispatches));
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<?> calcDeliveryPathAsGeoJson(@RequestBody List<MedDispatchRec> dispatches){
        return ResponseEntity.ok(pathfindingService.calcDeliveryPathAsGeoJson(dispatches));
    }

    //TODO: remove this helper function
    /**
     * helper used to display the restricted areas as well as calculated delivery route as GeoJson
     * @param dispatches
     * @return
     * @throws Exception
     */
    @PostMapping("/testPathWithObstacles")
    public ResponseEntity<String> testPathWithObstacles(
            @RequestBody List<MedDispatchRec> dispatches) throws Exception {

        // Get flight path
        String flightPathGeoJson = pathfindingService.calcDeliveryPathAsGeoJson(dispatches);
        List<RestrictedArea> restrictedAreas = externalAPIService.getRestrictedAreas();

        // Parse flight path
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> flightPathData = mapper.readValue(flightPathGeoJson, Map.class);
        List<List<Double>> flightCoordinates = (List<List<Double>>) flightPathData.get("coordinates");

        // Build FeatureCollection
        List<Map<String, Object>> features = new ArrayList<>();

        // 1. Add flight path as blue LineString
        Map<String, Object> flightFeature = new LinkedHashMap<>();
        flightFeature.put("type", "Feature");

        Map<String, Object> flightGeometry = new LinkedHashMap<>();
        flightGeometry.put("type", "LineString");
        flightGeometry.put("coordinates", flightCoordinates);
        flightFeature.put("geometry", flightGeometry);

        Map<String, Object> flightProps = new LinkedHashMap<>();
        flightProps.put("name", "Flight Path");
        flightProps.put("stroke", "#0000FF");  // Blue
        flightProps.put("stroke-width", 3);
        flightProps.put("stroke-opacity", 1);
        flightFeature.put("properties", flightProps);

        features.add(flightFeature);

        // 2. Add restricted areas as red Polygons
        for (RestrictedArea area : restrictedAreas) {
            Map<String, Object> areaFeature = new LinkedHashMap<>();
            areaFeature.put("type", "Feature");

            // Convert vertices to GeoJSON polygon coordinates
            List<List<List<Double>>> polygonCoords = new ArrayList<>();
            List<List<Double>> ring = new ArrayList<>();

            for (Position vertex : area.getVertices()) {
                List<Double> coord = new ArrayList<>();
                coord.add(vertex.getLng());
                coord.add(vertex.getLat());
                ring.add(coord);
            }

            // Close polygon (first point = last point)
            if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1))) {
                ring.add(ring.get(0));
            }

            polygonCoords.add(ring);

            Map<String, Object> areaGeometry = new LinkedHashMap<>();
            areaGeometry.put("type", "Polygon");
            areaGeometry.put("coordinates", polygonCoords);
            areaFeature.put("geometry", areaGeometry);

            Map<String, Object> areaProps = new LinkedHashMap<>();
            areaProps.put("name", area.getName());
            areaProps.put("fill", "#FF0000");  // Red fill
            areaProps.put("fill-opacity", 0.3);
            areaProps.put("stroke", "#FF0000");  // Red border
            areaProps.put("stroke-width", 2);
            areaFeature.put("properties", areaProps);

            features.add(areaFeature);
        }

        Map<String, Object> featureCollection = new LinkedHashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);

        String result = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(featureCollection);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @GetMapping("/restrictedAreas")
    public ResponseEntity<List<RestrictedArea>> getRestrictedAreas(){
        return ResponseEntity.ok(externalAPIService.getRestrictedAreas());
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
