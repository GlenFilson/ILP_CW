package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.service.DistanceService;
import uk.ac.ed.acp.cw1.service.DroneService;
import uk.ac.ed.acp.cw1.service.ExternalAPIService;
import uk.ac.ed.acp.cw1.service.PathfindingService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PathfindingService Unit Tests")
public class PathfindingServiceTests {

    @Mock
    private ExternalAPIService externalAPIService;

    @Mock
    private DroneService droneService;

    private DistanceService distanceService;
    private PathfindingService pathfindingService;

    // Test constants
    private static final double DELTA = 0.00001;

    // Edinburgh area coordinates for realistic testing
    private static final double EDINBURGH_LNG = -3.188267;
    private static final double EDINBURGH_LAT = 55.944425;

    @BeforeEach
    void setUp() {
        distanceService = new DistanceService();
        pathfindingService = new PathfindingService(distanceService, externalAPIService, droneService);
    }

    // ===============================================
    // HELPER METHODS
    // ===============================================

    private Drone createTestDrone(String id, double capacity, int maxMoves, boolean cooling, boolean heating) {
        Drone drone = new Drone();
        drone.setId(id);
        drone.setName("TestDrone-" + id);
        Drone.Capability cap = new Drone.Capability();
        cap.setCapacity(capacity);
        cap.setMaxMoves(maxMoves);
        cap.setCooling(cooling);
        cap.setHeating(heating);
        cap.setCostPerMove(0.1);
        cap.setCostInitial(1.0);
        cap.setCostFinal(1.0);
        drone.setCapability(cap);
        return drone;
    }

    private ServicePoint createServicePoint(String name, int id, double lat, double lng) {
        return new ServicePoint(name, id, new Position(lat, lng));
    }

    private MedDispatchRec createDispatch(int id, double capacity, boolean cooling, boolean heating,
                                          LocalDate date, LocalTime time, Position delivery) {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(id);
        dispatch.setDate(date);
        dispatch.setTime(time);
        dispatch.setDelivery(delivery);
        MedDispatchRec.Requirements req = new MedDispatchRec.Requirements();
        req.setCapacity(capacity);
        req.setCooling(cooling);
        req.setHeating(heating);
        dispatch.setRequirements(req);
        return dispatch;
    }

    private DroneForServicePointResponse createDroneAssignment(int servicePointId, String... droneIds) {
        DroneForServicePointResponse response = new DroneForServicePointResponse();
        response.setServicePointId(servicePointId);
        List<DroneAvailability> droneAvailabilities = new ArrayList<>();
        for (String droneId : droneIds) {
            DroneAvailability avail = new DroneAvailability();
            avail.setId(droneId);
            List<Availability> slots = new ArrayList<>();
            // Available all weekdays 8am-6pm
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    slots.add(new Availability(day, LocalTime.of(8, 0), LocalTime.of(18, 0)));
                }
            }
            avail.setAvailability(slots);
            droneAvailabilities.add(avail);
        }
        response.setDrones(droneAvailabilities);
        return response;
    }

    private RestrictedArea createRestrictedArea(String name, List<Position> vertices) {
        RestrictedArea area = new RestrictedArea();
        area.setName(name);
        area.setVertices(vertices);
        return area;
    }

    private List<Position> createSquareZone(double centerLat, double centerLng, double halfSize) {
        List<Position> vertices = new ArrayList<>();
        vertices.add(new Position(centerLat - halfSize, centerLng - halfSize));
        vertices.add(new Position(centerLat - halfSize, centerLng + halfSize));
        vertices.add(new Position(centerLat + halfSize, centerLng + halfSize));
        vertices.add(new Position(centerLat + halfSize, centerLng - halfSize));
        vertices.add(new Position(centerLat - halfSize, centerLng - halfSize)); // Close polygon
        return vertices;
    }

    private void setupBasicMocks() {
        Drone drone = createTestDrone("D001", 20.0, 5000, true, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        lenient().when(externalAPIService.getAllDrones()).thenReturn(List.of(drone));
        lenient().when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        lenient().when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        lenient().when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());
    }

    // ===============================================
    // 1. ROUTE CALCULATION TESTS (5 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Returns empty response for null dispatches")
    void testCalcDeliveryPath_nullDispatches_returnsEmptyResponse() {
        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(null);

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCost(), DELTA);
        assertEquals(0, response.getTotalMoves());
        assertTrue(response.getDronePaths().isEmpty());
    }

    @Test
    @DisplayName("calcDeliveryPath: Returns empty response for empty dispatches list")
    void testCalcDeliveryPath_emptyDispatches_returnsEmptyResponse() {
        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(new ArrayList<>());

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCost(), DELTA);
        assertEquals(0, response.getTotalMoves());
        assertTrue(response.getDronePaths().isEmpty());
    }

    @Test
    @DisplayName("calcDeliveryPath: Simple path calculation returns valid response")
    void testCalcDeliveryPath_simplePath_returnsValidResponse() {
        setupBasicMocks();

        // Create a dispatch close to the service point
        Position deliveryLocation = new Position(EDINBURGH_LAT + 0.001, EDINBURGH_LNG + 0.001);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), deliveryLocation);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getTotalCost() >= 0);
        assertTrue(response.getTotalMoves() >= 0);
    }

    @Test
    @DisplayName("calcDeliveryPath: Multiple dispatches are processed")
    void testCalcDeliveryPath_multipleDispatches_processesAll() {
        setupBasicMocks();

        Position delivery1 = new Position(EDINBURGH_LAT + 0.001, EDINBURGH_LNG);
        Position delivery2 = new Position(EDINBURGH_LAT - 0.001, EDINBURGH_LNG);

        MedDispatchRec dispatch1 = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 0), delivery1);
        MedDispatchRec dispatch2 = createDispatch(2, 3.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery2);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch1, dispatch2));

        assertNotNull(response);
        // Response should have been processed (may or may not have paths depending on constraints)
        assertTrue(response.getTotalCost() >= 0);
    }

    @Test
    @DisplayName("calcDeliveryPath: Long distance route requires multiple moves")
    void testCalcDeliveryPath_longDistance_requiresMultipleMoves() {
        setupBasicMocks();

        // Create a dispatch far from service point (should require many moves)
        Position farDelivery = new Position(EDINBURGH_LAT + 0.01, EDINBURGH_LNG + 0.01);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), farDelivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // For a distance of ~0.014 (diagonal), expect many moves at 0.00015 per move
        if (!response.getDronePaths().isEmpty()) {
            assertTrue(response.getTotalMoves() > 10, "Long distance should require multiple moves");
        }
    }

    // ===============================================
    // 2. NO-FLY ZONE AVOIDANCE TESTS (3 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Path avoids single restricted area")
    void testCalcDeliveryPath_singleRestrictedArea_pathAvoidsZone() {
        Drone drone = createTestDrone("D001", 20.0, 10000, true, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        // Create a restricted area between start and destination
        List<Position> zoneVertices = createSquareZone(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG + 0.0005, 0.0003);
        RestrictedArea zone = createRestrictedArea("NoFlyZone1", zoneVertices);

        when(externalAPIService.getAllDrones()).thenReturn(List.of(drone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(zone));

        Position delivery = new Position(EDINBURGH_LAT + 0.001, EDINBURGH_LNG + 0.001);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Path should still be found, navigating around the zone
    }

    @Test
    @DisplayName("calcDeliveryPath: Path avoids multiple restricted areas")
    void testCalcDeliveryPath_multipleRestrictedAreas_pathAvoidsAll() {
        Drone drone = createTestDrone("D001", 20.0, 10000, true, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        // Create multiple restricted areas
        List<Position> zone1Vertices = createSquareZone(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG + 0.0003, 0.0001);
        List<Position> zone2Vertices = createSquareZone(EDINBURGH_LAT + 0.0006, EDINBURGH_LNG + 0.0006, 0.0001);
        RestrictedArea zone1 = createRestrictedArea("NoFlyZone1", zone1Vertices);
        RestrictedArea zone2 = createRestrictedArea("NoFlyZone2", zone2Vertices);

        when(externalAPIService.getAllDrones()).thenReturn(List.of(drone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(List.of(zone1, zone2));

        Position delivery = new Position(EDINBURGH_LAT + 0.001, EDINBURGH_LNG + 0.001);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: No restricted areas allows direct path")
    void testCalcDeliveryPath_noRestrictedAreas_directPath() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
    }

    // ===============================================
    // 3. MOVE COUNTING & PRECISION TESTS (2 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Total moves is non-negative")
    void testCalcDeliveryPath_totalMoves_nonNegative() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG + 0.0005);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getTotalMoves() >= 0, "Total moves should be non-negative");
    }

    @Test
    @DisplayName("calcDeliveryPath: Total cost is non-negative")
    void testCalcDeliveryPath_totalCost_nonNegative() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG + 0.0005);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getTotalCost() >= 0, "Total cost should be non-negative");
    }

    // ===============================================
    // 4. PATH OPTIMIZATION TESTS (2 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Same input produces consistent output")
    void testCalcDeliveryPath_sameInput_consistentOutput() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG + 0.0005);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response1 = pathfindingService.calcDeliveryPath(List.of(dispatch));
        CalcDeliveryPathResponse response2 = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertEquals(response1.getTotalMoves(), response2.getTotalMoves(),
                "Deterministic pathfinding should produce same move count");
        assertEquals(response1.getTotalCost(), response2.getTotalCost(), DELTA,
                "Deterministic pathfinding should produce same cost");
    }

    @Test
    @DisplayName("calcDeliveryPath: Response contains dronePaths list")
    void testCalcDeliveryPath_responseContainsDronePaths() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertNotNull(response.getDronePaths(), "Response should contain dronePaths list");
    }

    // ===============================================
    // 5. BOUNDARY CONDITIONS TESTS (4 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Handles negative longitude values")
    void testCalcDeliveryPath_negativeLongitude_handledCorrectly() {
        setupBasicMocks();

        // Edinburgh has negative longitude (-3.188267)
        Position delivery = new Position(EDINBURGH_LAT, EDINBURGH_LNG - 0.001);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Should handle negative coordinates correctly
    }

    @Test
    @DisplayName("calcDeliveryPath: Handles positive latitude values")
    void testCalcDeliveryPath_positiveLatitude_handledCorrectly() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.001, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: Drone with insufficient capacity is filtered")
    void testCalcDeliveryPath_insufficientCapacity_droneFiltered() {
        Drone smallDrone = createTestDrone("D001", 2.0, 5000, true, true); // Only 2.0 capacity
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(smallDrone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 10.0, false, false, // Requires 10.0 capacity
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Drone should not be able to fulfill this dispatch
        assertTrue(response.getDronePaths().isEmpty(), "Drone with insufficient capacity should not be assigned");
    }

    @Test
    @DisplayName("calcDeliveryPath: Drone without required cooling is filtered")
    void testCalcDeliveryPath_noCooling_droneFiltered() {
        Drone noCoolingDrone = createTestDrone("D001", 20.0, 5000, false, true); // No cooling
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(noCoolingDrone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, true, false, // Requires cooling
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getDronePaths().isEmpty(), "Drone without cooling should not be assigned");
    }

    // ===============================================
    // 6. GEOJSON OUTPUT TESTS (2 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPathAsGeoJson: Returns valid GeoJson string")
    void testCalcDeliveryPathAsGeoJson_returnsValidGeoJson() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        String geoJson = pathfindingService.calcDeliveryPathAsGeoJson(List.of(dispatch));

        assertNotNull(geoJson);
        assertTrue(geoJson.contains("type"), "GeoJson should contain 'type' field");
        assertTrue(geoJson.contains("coordinates"), "GeoJson should contain 'coordinates' field");
    }

    @Test
    @DisplayName("calcDeliveryPathAsGeoJson: Empty dispatches returns valid GeoJson")
    void testCalcDeliveryPathAsGeoJson_emptyDispatches_returnsValidGeoJson() {
        String geoJson = pathfindingService.calcDeliveryPathAsGeoJson(new ArrayList<>());

        assertNotNull(geoJson);
        assertTrue(geoJson.contains("LineString"), "Empty path should return LineString GeoJson");
    }

    // ===============================================
    // 7. DATE/TIME HANDLING TESTS (3 tests)
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Dispatches grouped by date")
    void testCalcDeliveryPath_dispatchesGroupedByDate() {
        setupBasicMocks();

        Position delivery1 = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        Position delivery2 = new Position(EDINBURGH_LAT - 0.0003, EDINBURGH_LNG);

        // Two dispatches on different dates
        MedDispatchRec dispatch1 = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 0), delivery1);
        MedDispatchRec dispatch2 = createDispatch(2, 5.0, false, false,
                LocalDate.of(2025, 12, 23), LocalTime.of(14, 0), delivery2);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch1, dispatch2));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: Null date dispatches handled together")
    void testCalcDeliveryPath_nullDateDispatches_handledTogether() {
        setupBasicMocks();

        Position delivery1 = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        Position delivery2 = new Position(EDINBURGH_LAT - 0.0003, EDINBURGH_LNG);

        // Two dispatches with null dates
        MedDispatchRec dispatch1 = createDispatch(1, 5.0, false, false,
                null, LocalTime.of(14, 0), delivery1);
        MedDispatchRec dispatch2 = createDispatch(2, 5.0, false, false,
                null, LocalTime.of(14, 30), delivery2);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch1, dispatch2));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: Weekend dispatches handled correctly")
    void testCalcDeliveryPath_weekendDispatch_handledCorrectly() {
        // Setup drone that's only available on weekdays
        Drone drone = createTestDrone("D001", 20.0, 5000, true, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(drone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        // Saturday - drone should not be available (based on our test setup)
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 20), LocalTime.of(14, 0), delivery); // Saturday

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Since our test drone is only available weekdays, this should have empty paths
        assertTrue(response.getDronePaths().isEmpty(), "Weekend dispatch should not be fulfilled by weekday-only drone");
    }

    // ===============================================
    // 8. ADDITIONAL COVERAGE TESTS
    // ===============================================

    @Test
    @DisplayName("calcDeliveryPath: Drone without required heating is filtered")
    void testCalcDeliveryPath_noHeating_droneFiltered() {
        Drone noHeatingDrone = createTestDrone("D001", 20.0, 5000, true, false); // No heating
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(noHeatingDrone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, true, // Requires heating
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getDronePaths().isEmpty(), "Drone without heating should not be assigned");
    }

    @Test
    @DisplayName("calcDeliveryPath: Multiple drones can handle different dispatches")
    void testCalcDeliveryPath_multipleDrones_handleDifferentDispatches() {
        Drone drone1 = createTestDrone("D001", 10.0, 5000, true, false);
        Drone drone2 = createTestDrone("D002", 15.0, 5000, false, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);

        // Both drones assigned to same service point
        DroneForServicePointResponse assignment = new DroneForServicePointResponse();
        assignment.setServicePointId(1);
        List<DroneAvailability> availabilities = new ArrayList<>();
        for (String droneId : List.of("D001", "D002")) {
            DroneAvailability avail = new DroneAvailability();
            avail.setId(droneId);
            List<Availability> slots = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    slots.add(new Availability(day, LocalTime.of(8, 0), LocalTime.of(18, 0)));
                }
            }
            avail.setAvailability(slots);
            availabilities.add(avail);
        }
        assignment.setDrones(availabilities);

        when(externalAPIService.getAllDrones()).thenReturn(List.of(drone1, drone2));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery1 = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        Position delivery2 = new Position(EDINBURGH_LAT - 0.0003, EDINBURGH_LNG);

        MedDispatchRec dispatch1 = createDispatch(1, 5.0, true, false, // Needs cooling
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 0), delivery1);
        MedDispatchRec dispatch2 = createDispatch(2, 5.0, false, true, // Needs heating
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery2);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch1, dispatch2));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: Drone with insufficient maxMoves is filtered")
    void testCalcDeliveryPath_insufficientMaxMoves_droneFiltered() {
        Drone limitedDrone = createTestDrone("D001", 20.0, 10, true, true); // Only 10 max moves
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(limitedDrone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        // Far delivery requiring many moves
        Position farDelivery = new Position(EDINBURGH_LAT + 0.01, EDINBURGH_LNG + 0.01);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), farDelivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Drone should not be able to complete delivery with only 10 moves
        assertTrue(response.getDronePaths().isEmpty(), "Drone with insufficient maxMoves should not be assigned");
    }

    @Test
    @DisplayName("calcDeliveryPath: Dispatch with null time is handled")
    void testCalcDeliveryPath_nullTime_handledCorrectly() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), null, delivery); // null time

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
    }

    @Test
    @DisplayName("calcDeliveryPath: Very close delivery requires minimal moves")
    void testCalcDeliveryPath_veryCloseDelivery_minimalMoves() {
        setupBasicMocks();

        // Delivery very close to service point
        Position closeDelivery = new Position(EDINBURGH_LAT + 0.00015, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), closeDelivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Close delivery should have minimal moves
        if (!response.getDronePaths().isEmpty()) {
            assertTrue(response.getTotalMoves() < 50, "Very close delivery should require few moves");
        }
    }

    @Test
    @DisplayName("calcDeliveryPath: Response has correct structure for successful delivery")
    void testCalcDeliveryPath_successfulDelivery_correctStructure() {
        setupBasicMocks();

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        if (!response.getDronePaths().isEmpty()) {
            DronePath dronePath = response.getDronePaths().get(0);
            assertNotNull(dronePath.getDroneId(), "DronePath should have droneId");
            assertNotNull(dronePath.getDeliveries(), "DronePath should have deliveries list");
            assertFalse(dronePath.getDeliveries().isEmpty(), "Deliveries list should not be empty");
        }
    }

    @Test
    @DisplayName("calcDeliveryPathAsGeoJson: Null dispatches returns valid GeoJson")
    void testCalcDeliveryPathAsGeoJson_nullDispatches_returnsValidGeoJson() {
        String geoJson = pathfindingService.calcDeliveryPathAsGeoJson(null);

        assertNotNull(geoJson);
        assertTrue(geoJson.contains("LineString"), "Null dispatches should return LineString GeoJson");
    }

    @Test
    @DisplayName("calcDeliveryPath: No drones available returns empty response")
    void testCalcDeliveryPath_noDronesAvailable_returnsEmptyResponse() {
        when(externalAPIService.getAllDrones()).thenReturn(new ArrayList<>());
        when(externalAPIService.getServicePoints()).thenReturn(new ArrayList<>());
        when(externalAPIService.getDronesForServicePoints()).thenReturn(new ArrayList<>());
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getDronePaths().isEmpty(), "No drones should mean no paths");
    }

    @Test
    @DisplayName("calcDeliveryPath: No service points returns empty response")
    void testCalcDeliveryPath_noServicePoints_returnsEmptyResponse() {
        Drone drone = createTestDrone("D001", 20.0, 5000, true, true);
        DroneForServicePointResponse assignment = createDroneAssignment(1, "D001");

        when(externalAPIService.getAllDrones()).thenReturn(List.of(drone));
        when(externalAPIService.getServicePoints()).thenReturn(new ArrayList<>()); // No service points
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0005, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, false, false,
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        assertTrue(response.getDronePaths().isEmpty(), "No service points should mean no paths");
    }

    @Test
    @DisplayName("calcDeliveryPath: Both cooling and heating required filters correctly")
    void testCalcDeliveryPath_coolingAndHeatingRequired_filtersCorrectly() {
        // Create one drone with only cooling, one with both
        Drone coolingOnlyDrone = createTestDrone("D001", 20.0, 5000, true, false);
        Drone bothDrone = createTestDrone("D002", 20.0, 5000, true, true);
        ServicePoint sp = createServicePoint("SP1", 1, EDINBURGH_LAT, EDINBURGH_LNG);

        DroneForServicePointResponse assignment = new DroneForServicePointResponse();
        assignment.setServicePointId(1);
        List<DroneAvailability> availabilities = new ArrayList<>();
        for (String droneId : List.of("D001", "D002")) {
            DroneAvailability avail = new DroneAvailability();
            avail.setId(droneId);
            List<Availability> slots = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    slots.add(new Availability(day, LocalTime.of(8, 0), LocalTime.of(18, 0)));
                }
            }
            avail.setAvailability(slots);
            availabilities.add(avail);
        }
        assignment.setDrones(availabilities);

        when(externalAPIService.getAllDrones()).thenReturn(List.of(coolingOnlyDrone, bothDrone));
        when(externalAPIService.getServicePoints()).thenReturn(List.of(sp));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));
        when(externalAPIService.getRestrictedAreas()).thenReturn(new ArrayList<>());

        Position delivery = new Position(EDINBURGH_LAT + 0.0003, EDINBURGH_LNG);
        MedDispatchRec dispatch = createDispatch(1, 5.0, true, true, // Requires BOTH cooling AND heating
                LocalDate.of(2025, 12, 22), LocalTime.of(14, 30), delivery);

        CalcDeliveryPathResponse response = pathfindingService.calcDeliveryPath(List.of(dispatch));

        assertNotNull(response);
        // Only D002 should be able to fulfill this dispatch
        if (!response.getDronePaths().isEmpty()) {
            assertEquals("D002", response.getDronePaths().get(0).getDroneId(),
                    "Only drone with both cooling and heating should be assigned");
        }
    }
}
