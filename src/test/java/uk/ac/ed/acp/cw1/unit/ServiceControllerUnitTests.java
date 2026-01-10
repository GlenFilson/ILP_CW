package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.ac.ed.acp.cw1.controller.ServiceController;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.service.DistanceService;
import uk.ac.ed.acp.cw1.service.DroneService;
import uk.ac.ed.acp.cw1.service.ExternalAPIService;
import uk.ac.ed.acp.cw1.service.PathfindingService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceController Unit Tests")
public class ServiceControllerUnitTests {

    @Mock
    private DistanceService distanceService;

    @Mock
    private DroneService droneService;

    @Mock
    private ExternalAPIService externalAPIService;

    @Mock
    private PathfindingService pathfindingService;

    @InjectMocks
    private ServiceController serviceController;


    @Test
    @DisplayName("uid: Returns correct student ID")
    void testUid_returnsCorrectStudentId() {
        String result = serviceController.uid();
        assertEquals("s2539057", result);
    }


    @Test
    @DisplayName("distanceTo: Returns distance from service")
    void testDistanceTo_returnsDistance() {
        Position p1 = new Position(55.0, -3.0);
        Position p2 = new Position(55.1, -3.1);
        DistanceToRequest request = new DistanceToRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        when(distanceService.euclideanDistance(p1, p2)).thenReturn(0.14142);

        ResponseEntity<?> response = serviceController.distanceTo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.14142, response.getBody());
        verify(distanceService).euclideanDistance(p1, p2);
    }


    @Test
    @DisplayName("isCloseTo: Returns true when positions are close")
    void testIsCloseTo_returnsTrue_whenClose() {
        Position p1 = new Position(55.0, -3.0);
        Position p2 = new Position(55.0001, -3.0001);
        IsCloseToRequest request = new IsCloseToRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        when(distanceService.isCloseTo(p1, p2)).thenReturn(true);

        ResponseEntity<?> response = serviceController.isCloseTo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    @DisplayName("isCloseTo: Returns false when positions are not close")
    void testIsCloseTo_returnsFalse_whenNotClose() {
        Position p1 = new Position(55.0, -3.0);
        Position p2 = new Position(56.0, -4.0);
        IsCloseToRequest request = new IsCloseToRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        when(distanceService.isCloseTo(p1, p2)).thenReturn(false);

        ResponseEntity<?> response = serviceController.isCloseTo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }


    @Test
    @DisplayName("nextPosition: Returns next position from service")
    void testNextPosition_returnsNextPosition() {
        Position start = new Position(55.0, -3.0);
        Double angle = 90.0;
        Position nextPos = new Position(55.0, -2.99985);

        NextPositionRequest request = new NextPositionRequest();
        request.setStart(start);
        request.setAngle(angle);

        when(distanceService.nextPosition(start, angle)).thenReturn(nextPos);

        ResponseEntity<?> response = serviceController.nextPosition(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof NextPositionResponse);

        NextPositionResponse result = (NextPositionResponse) response.getBody();
        assertEquals(nextPos.getLat(), result.getLat());
        assertEquals(nextPos.getLng(), result.getLng());
    }


    @Test
    @DisplayName("isInRegion: Returns true when position is in region")
    void testIsInRegion_returnsTrue_whenInRegion() {
        Position position = new Position(55.5, -3.5);
        List<Position> vertices = List.of(
                new Position(55.0, -4.0),
                new Position(55.0, -3.0),
                new Position(56.0, -3.0),
                new Position(56.0, -4.0),
                new Position(55.0, -4.0)
        );
        Region region = new Region("TestRegion", vertices);

        IsInRegionRequest request = new IsInRegionRequest();
        request.setPosition(position);
        request.setRegion(region);

        when(distanceService.isInRegion(position, region)).thenReturn(true);

        ResponseEntity<?> response = serviceController.isInRegion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    @DisplayName("isInRegion: Returns false when position is not in region")
    void testIsInRegion_returnsFalse_whenNotInRegion() {
        Position position = new Position(60.0, -10.0);
        List<Position> vertices = List.of(
                new Position(55.0, -4.0),
                new Position(55.0, -3.0),
                new Position(56.0, -3.0),
                new Position(56.0, -4.0),
                new Position(55.0, -4.0)
        );
        Region region = new Region("TestRegion", vertices);

        IsInRegionRequest request = new IsInRegionRequest();
        request.setPosition(position);
        request.setRegion(region);

        when(distanceService.isInRegion(position, region)).thenReturn(false);

        ResponseEntity<?> response = serviceController.isInRegion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }


    @Test
    @DisplayName("dronesWithCooling: Returns drones with cooling=true")
    void testDronesWithCooling_true_returnsDrones() {
        List<String> expectedDrones = List.of("D001", "D003");
        when(droneService.getDronesWithCooling(true)).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.dronesWithCooling(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDrones, response.getBody());
    }

    @Test
    @DisplayName("dronesWithCooling: Returns drones with cooling=false")
    void testDronesWithCooling_false_returnsDrones() {
        List<String> expectedDrones = List.of("D002");
        when(droneService.getDronesWithCooling(false)).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.dronesWithCooling(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDrones, response.getBody());
    }


    @Test
    @DisplayName("droneDetails: Returns drone when found")
    void testDroneDetails_found_returnsDrone() {
        Drone drone = new Drone();
        drone.setId("D001");
        drone.setName("TestDrone");

        when(droneService.getDroneById("D001")).thenReturn(drone);

        ResponseEntity<?> response = serviceController.droneDetails("D001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(drone, response.getBody());
    }

    @Test
    @DisplayName("droneDetails: Returns 404 when drone not found")
    void testDroneDetails_notFound_returns404() {
        when(droneService.getDroneById("D999")).thenReturn(null);

        ResponseEntity<?> response = serviceController.droneDetails("D999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    @DisplayName("queryAsPath: Returns matching drones")
    void testQueryAsPath_returnsMatchingDrones() {
        List<String> expectedDrones = List.of("D001", "D002");
        when(droneService.queryAsPath("capacity", "15")).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.queryAsPath("capacity", "15");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDrones, response.getBody());
    }

    @Test
    @DisplayName("queryAsPath: Returns empty list for no matches")
    void testQueryAsPath_noMatches_returnsEmptyList() {
        when(droneService.queryAsPath("invalidAttr", "value")).thenReturn(new ArrayList<>());

        ResponseEntity<List<String>> response = serviceController.queryAsPath("invalidAttr", "value");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }


    @Test
    @DisplayName("query: Returns matching drones for query attributes")
    void testQuery_returnsMatchingDrones() {
        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("cooling", "=", "true")
        );
        List<String> expectedDrones = List.of("D001", "D003");
        when(droneService.query(attributes)).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.query(attributes);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDrones, response.getBody());
    }

    @Test
    @DisplayName("query: Returns all drones for empty attributes")
    void testQuery_emptyAttributes_returnsAllDrones() {
        List<QueryAttribute> attributes = new ArrayList<>();
        List<String> expectedDrones = List.of("D001", "D002", "D003");
        when(droneService.query(attributes)).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.query(attributes);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
    }


    @Test
    @DisplayName("queryAvailableDrones: Returns available drones")
    void testQueryAvailableDrones_returnsAvailableDrones() {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        List<String> expectedDrones = List.of("D001");
        when(droneService.queryAvailableDrones(dispatches)).thenReturn(expectedDrones);

        ResponseEntity<List<String>> response = serviceController.queryAvailableDrones(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDrones, response.getBody());
    }


    @Test
    @DisplayName("calcDeliveryPath: Returns delivery path response")
    void testCalcDeliveryPath_returnsDeliveryPath() {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        CalcDeliveryPathResponse expectedResponse = new CalcDeliveryPathResponse();
        expectedResponse.setTotalCost(10.0);
        expectedResponse.setTotalMoves(50);
        expectedResponse.setDronePaths(new ArrayList<>());

        when(pathfindingService.calcDeliveryPath(dispatches)).thenReturn(expectedResponse);

        ResponseEntity<?> response = serviceController.calcDeliveryPath(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }


    @Test
    @DisplayName("calcDeliveryPathAsGeoJson: Returns GeoJSON string")
    void testCalcDeliveryPathAsGeoJson_returnsGeoJson() {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        String expectedGeoJson = "{\"type\":\"LineString\",\"coordinates\":[]}";

        when(pathfindingService.calcDeliveryPathAsGeoJson(dispatches)).thenReturn(expectedGeoJson);

        ResponseEntity<?> response = serviceController.calcDeliveryPathAsGeoJson(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedGeoJson, response.getBody());
    }


    @Test
    @DisplayName("testPathWithObstacles: Returns FeatureCollection GeoJSON")
    void testTestPathWithObstacles_returnsFeatureCollection() throws Exception {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        String flightGeoJson = "{\"type\":\"LineString\",\"coordinates\":[[-3.0,55.0],[-3.1,55.1]]}";

        List<Position> vertices = List.of(
                new Position(55.0, -3.0),
                new Position(55.0, -3.1),
                new Position(55.1, -3.1),
                new Position(55.1, -3.0),
                new Position(55.0, -3.0)
        );
        RestrictedArea area = new RestrictedArea();
        area.setName("TestZone");
        area.setVertices(vertices);

        when(pathfindingService.calcDeliveryPathAsGeoJson(dispatches)).thenReturn(flightGeoJson);
        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(area));

        ResponseEntity<String> response = serviceController.testPathWithObstacles(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("FeatureCollection"));
        assertTrue(response.getBody().contains("Flight Path"));
        assertTrue(response.getBody().contains("TestZone"));
    }

    @Test
    @DisplayName("testPathWithObstacles: Handles empty restricted areas")
    void testTestPathWithObstacles_emptyRestrictedAreas() throws Exception {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        String flightGeoJson = "{\"type\":\"LineString\",\"coordinates\":[[-3.0,55.0],[-3.1,55.1]]}";

        when(pathfindingService.calcDeliveryPathAsGeoJson(dispatches)).thenReturn(flightGeoJson);
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        ResponseEntity<String> response = serviceController.testPathWithObstacles(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("FeatureCollection"));
        assertTrue(response.getBody().contains("Flight Path"));
    }

    @Test
    @DisplayName("testPathWithObstacles: Handles multiple restricted areas")
    void testTestPathWithObstacles_multipleRestrictedAreas() throws Exception {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        String flightGeoJson = "{\"type\":\"LineString\",\"coordinates\":[[-3.0,55.0],[-3.1,55.1]]}";

        List<Position> vertices1 = List.of(
                new Position(55.0, -3.0),
                new Position(55.0, -3.1),
                new Position(55.1, -3.1),
                new Position(55.0, -3.0)
        );
        RestrictedArea area1 = new RestrictedArea();
        area1.setName("Zone1");
        area1.setVertices(vertices1);

        List<Position> vertices2 = List.of(
                new Position(55.2, -3.2),
                new Position(55.2, -3.3),
                new Position(55.3, -3.3),
                new Position(55.2, -3.2)
        );
        RestrictedArea area2 = new RestrictedArea();
        area2.setName("Zone2");
        area2.setVertices(vertices2);

        when(pathfindingService.calcDeliveryPathAsGeoJson(dispatches)).thenReturn(flightGeoJson);
        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(area1, area2));

        ResponseEntity<String> response = serviceController.testPathWithObstacles(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Zone1"));
        assertTrue(response.getBody().contains("Zone2"));
    }

    @Test
    @DisplayName("testPathWithObstacles: Handles polygon that needs closing")
    void testTestPathWithObstacles_polygonNeedsClosure() throws Exception {
        List<MedDispatchRec> dispatches = List.of(new MedDispatchRec());
        String flightGeoJson = "{\"type\":\"LineString\",\"coordinates\":[[-3.0,55.0]]}";

        // Polygon where first != last (needs closing)
        List<Position> vertices = List.of(
                new Position(55.0, -3.0),
                new Position(55.0, -3.1),
                new Position(55.1, -3.1),
                new Position(55.1, -3.0)
                // Missing closure point
        );
        RestrictedArea area = new RestrictedArea();
        area.setName("UnclosedZone");
        area.setVertices(vertices);

        when(pathfindingService.calcDeliveryPathAsGeoJson(dispatches)).thenReturn(flightGeoJson);
        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(area));

        ResponseEntity<String> response = serviceController.testPathWithObstacles(dispatches);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("UnclosedZone"));
    }


    @Test
    @DisplayName("getRestrictedAreas: Returns restricted areas from service")
    void testGetRestrictedAreas_returnsAreas() {
        List<Position> vertices = List.of(
                new Position(55.0, -3.0),
                new Position(55.0, -3.1),
                new Position(55.1, -3.1),
                new Position(55.0, -3.0)
        );
        RestrictedArea area = new RestrictedArea();
        area.setName("TestZone");
        area.setVertices(vertices);

        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(area));

        ResponseEntity<List<RestrictedArea>> response = serviceController.getRestrictedAreas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("TestZone", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("getRestrictedAreas: Returns empty list when no areas")
    void testGetRestrictedAreas_noAreas_returnsEmptyList() {
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        ResponseEntity<List<RestrictedArea>> response = serviceController.getRestrictedAreas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }


    @Test
    @DisplayName("handleValidationExceptions: Logs field errors")
    void testHandleValidationExceptions_logsErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // This should not throw and should log the error
        assertDoesNotThrow(() -> serviceController.handleValidationExceptions(exception));
    }

    @Test
    @DisplayName("handleValidationExceptions: Handles multiple field errors")
    void testHandleValidationExceptions_multipleErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("object", "field1", "error 1");
        FieldError error2 = new FieldError("object", "field2", "error 2");
        when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        assertDoesNotThrow(() -> serviceController.handleValidationExceptions(exception));
    }
}

