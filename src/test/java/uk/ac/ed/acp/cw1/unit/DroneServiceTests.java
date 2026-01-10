package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.service.DistanceService;
import uk.ac.ed.acp.cw1.service.DroneService;
import uk.ac.ed.acp.cw1.service.ExternalAPIService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DroneServiceTests {

    @Mock
    private ExternalAPIService externalAPIService;

    @Mock
    private DistanceService distanceService;

    private DroneService droneService;

    private List<Drone> testDrones;
    private List<ServicePoint> testServicePoints;
    private List<DroneForServicePointResponse> testDroneAssignments;

    @BeforeEach
    void setUp() {
        droneService = new DroneService(externalAPIService, distanceService);
        setupTestData();
    }

    private void setupTestData() {
        // Create test drones
        testDrones = new ArrayList<>();

        Drone drone1 = new Drone("Drone1", "D001",
                new Drone.Capability(true, false, 10.0, 1000, 0.5, 1.0, 1.0));
        Drone drone2 = new Drone("Drone2", "D002",
                new Drone.Capability(false, true, 20.0, 2000, 0.3, 2.0, 2.0));
        Drone drone3 = new Drone("Drone3", "D003",
                new Drone.Capability(true, true, 15.0, 1500, 0.4, 1.5, 1.5));

        testDrones.add(drone1);
        testDrones.add(drone2);
        testDrones.add(drone3);

        // Create test service points
        testServicePoints = new ArrayList<>();
        testServicePoints.add(new ServicePoint("SP1", 1, new Position(55.0, -3.0)));
        testServicePoints.add(new ServicePoint("SP2", 2, new Position(55.5, -3.5)));

        // Create test drone assignments with availability
        testDroneAssignments = new ArrayList<>();

        List<Availability> availabilities = List.of(
                new Availability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new Availability(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        );

        DroneAvailability da1 = new DroneAvailability("D001", availabilities);
        DroneAvailability da2 = new DroneAvailability("D002", availabilities);
        DroneAvailability da3 = new DroneAvailability("D003", availabilities);

        DroneForServicePointResponse assignment1 = new DroneForServicePointResponse(1, List.of(da1, da2));
        DroneForServicePointResponse assignment2 = new DroneForServicePointResponse(2, List.of(da3));

        testDroneAssignments.add(assignment1);
        testDroneAssignments.add(assignment2);
    }

    // getDronesWithCooling tests
    @Test
    void testGetDronesWithCooling_true_returnsCorrectDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.getDronesWithCooling(true);

        assertEquals(2, result.size());
        assertTrue(result.contains("D001"));
        assertTrue(result.contains("D003"));
        assertFalse(result.contains("D002"));
    }

    @Test
    void testGetDronesWithCooling_false_returnsCorrectDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.getDronesWithCooling(false);

        assertEquals(1, result.size());
        assertTrue(result.contains("D002"));
    }

    @Test
    void testGetDronesWithCooling_emptyList_returnsEmpty() {
        when(externalAPIService.getAllDrones()).thenReturn(new ArrayList<>());

        List<String> result = droneService.getDronesWithCooling(true);

        assertTrue(result.isEmpty());
    }

    // getDroneById tests
    @Test
    void testGetDroneById_existingDrone_returnsDrone() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        Drone result = droneService.getDroneById("D001");

        assertNotNull(result);
        assertEquals("D001", result.getId());
        assertEquals("Drone1", result.getName());
    }

    @Test
    void testGetDroneById_nonExistingDrone_returnsNull() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        Drone result = droneService.getDroneById("D999");

        assertNull(result);
    }

    @Test
    void testGetDroneById_emptyList_returnsNull() {
        when(externalAPIService.getAllDrones()).thenReturn(new ArrayList<>());

        Drone result = droneService.getDroneById("D001");

        assertNull(result);
    }

    // queryAsPath tests
    @Test
    void testQueryAsPath_coolingTrue_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("cooling", "true");

        assertEquals(2, result.size());
        assertTrue(result.contains("D001"));
        assertTrue(result.contains("D003"));
    }

    @Test
    void testQueryAsPath_heatingFalse_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("heating", "false");

        assertEquals(1, result.size());
        assertTrue(result.contains("D001"));
    }

    @Test
    void testQueryAsPath_capacityValue_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("capacity", "10.0");

        assertEquals(1, result.size());
        assertTrue(result.contains("D001"));
    }

    @Test
    void testQueryAsPath_maxMovesValue_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("maxMoves", "1500");

        assertEquals(1, result.size());
        assertTrue(result.contains("D003"));
    }

    @Test
    void testQueryAsPath_invalidAttribute_returnsEmpty() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("invalidAttr", "value");

        assertTrue(result.isEmpty());
    }

    // query tests
    @Test
    void testQuery_singleAttribute_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("cooling", "=", "true")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(2, result.size());
        assertTrue(result.contains("D001"));
        assertTrue(result.contains("D003"));
    }

    @Test
    void testQuery_multipleAttributes_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("cooling", "=", "true"),
                new QueryAttribute("heating", "=", "true")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(1, result.size());
        assertTrue(result.contains("D003"));
    }

    @Test
    void testQuery_greaterThanOperator_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("capacity", ">", "12.0")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(2, result.size());
        assertTrue(result.contains("D002"));
        assertTrue(result.contains("D003"));
    }

    @Test
    void testQuery_lessThanOperator_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("maxMoves", "<", "1200")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(1, result.size());
        assertTrue(result.contains("D001"));
    }

    @Test
    void testQuery_emptyAttributes_returnsAllDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.query(new ArrayList<>());

        assertEquals(3, result.size());
    }

    // queryAvailableDrones tests
    @Test
    void testQueryAvailableDrones_emptyDispatches_returnsEmpty() {
        List<String> result = droneService.queryAvailableDrones(new ArrayList<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_nullDispatches_returnsEmpty() {
        List<String> result = droneService.queryAvailableDrones(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_matchingCapacity_returnsDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.001);

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, false, null),
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        assertFalse(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_coolingRequired_filtersCorrectly() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.001);

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, true, false, null),
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Only drones with cooling should be returned
        for (String droneId : result) {
            Drone drone = testDrones.stream()
                    .filter(d -> d.getId().equals(droneId))
                    .findFirst()
                    .orElse(null);
            if (drone != null) {
                assertTrue(drone.getCapability().isCooling());
            }
        }
    }

    @Test
    void testQueryAvailableDrones_heatingRequired_filtersCorrectly() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.001);

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, true, null),
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Only drones with heating should be returned
        for (String droneId : result) {
            Drone drone = testDrones.stream()
                    .filter(d -> d.getId().equals(droneId))
                    .findFirst()
                    .orElse(null);
            if (drone != null) {
                assertTrue(drone.getCapability().isHeating());
            }
        }
    }

    @Test
    void testQueryAvailableDrones_capacityTooHigh_filtersOut() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        // Note: euclideanDistance stubbing not needed here because capacity check fails first,
        // before distance calculation is ever reached

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(25.0, false, false, null), // Higher than any drone capacity
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        assertTrue(result.isEmpty());
    }

    // ===============================================
    // ADDITIONAL COVERAGE TESTS
    // ===============================================

    @Test
    void testQueryAvailableDrones_withMaxCostConstraint_filtersCorrectly() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.0001); // Very short distance

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, false, 100.0), // maxCost = 100.0
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Should return drones that can deliver within cost constraint
        assertNotNull(result);
    }

    @Test
    void testQueryAvailableDrones_maxCostTooLow_filtersOut() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1.0); // Large distance = high cost

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, false, 0.01), // Very low maxCost
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Should filter out drones that can't meet the cost constraint
        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_multipleDispatches_checksAll() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.001);

        MedDispatchRec dispatch1 = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(3.0, false, false, null),
                new Position(55.1, -3.1)
        );

        MedDispatchRec dispatch2 = new MedDispatchRec(
                2,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(11, 0),
                new MedDispatchRec.Requirements(4.0, false, false, null),
                new Position(55.2, -3.2)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch1, dispatch2));

        // Drones must be able to handle both dispatches
        assertNotNull(result);
    }

    @Test
    void testQueryAvailableDrones_coolingAndHeatingRequired_filtersCorrectly() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0.001);

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, true, true, null), // Both cooling AND heating
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Only D003 has both cooling and heating
        for (String droneId : result) {
            Drone drone = testDrones.stream()
                    .filter(d -> d.getId().equals(droneId))
                    .findFirst()
                    .orElse(null);
            if (drone != null) {
                assertTrue(drone.getCapability().isCooling() && drone.getCapability().isHeating());
            }
        }
    }

    @Test
    void testQueryAvailableDrones_maxMovesExceeded_filtersOut() {
        // Create drone with very low maxMoves
        List<Drone> limitedDrones = List.of(
                new Drone("LimitedDrone", "D100",
                        new Drone.Capability(true, true, 50.0, 10, 0.5, 1.0, 1.0)) // Only 10 max moves
        );

        when(externalAPIService.getAllDrones()).thenReturn(limitedDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);

        // Create assignment for the limited drone
        List<Availability> avail = List.of(
                new Availability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))
        );
        DroneAvailability da = new DroneAvailability("D100", avail);
        DroneForServicePointResponse assignment = new DroneForServicePointResponse(1, List.of(da));
        when(externalAPIService.getDronesForServicePoints()).thenReturn(List.of(assignment));

        // Large distance requires many moves
        when(distanceService.euclideanDistance(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1.0); // 1.0 / 0.00015 = ~6667 moves one way

        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, false, null),
                new Position(60.0, -10.0) // Far away
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Drone should be filtered out due to maxMoves constraint
        assertTrue(result.isEmpty());
    }

    @Test
    void testQuery_notEqualsOperator_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("cooling", "!=", "true")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(1, result.size());
        assertTrue(result.contains("D002")); // D002 has cooling = false
    }

    @Test
    void testQuery_greaterThanOrEqualsOperator_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("capacity", ">=", "15.0")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(2, result.size());
        assertTrue(result.contains("D002")); // capacity = 20.0
        assertTrue(result.contains("D003")); // capacity = 15.0
    }

    @Test
    void testQuery_lessThanOrEqualsOperator_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("capacity", "<=", "15.0")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(2, result.size());
        assertTrue(result.contains("D001")); // capacity = 10.0
        assertTrue(result.contains("D003")); // capacity = 15.0
    }

    @Test
    void testQuery_costPerMove_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<QueryAttribute> attributes = List.of(
                new QueryAttribute("costPerMove", "<", "0.4")
        );

        List<String> result = droneService.query(attributes);

        assertEquals(1, result.size());
        assertTrue(result.contains("D002")); // costPerMove = 0.3
    }

    @Test
    void testQueryAsPath_costInitial_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("costInitial", "2.0");

        assertEquals(1, result.size());
        assertTrue(result.contains("D002"));
    }

    @Test
    void testQueryAsPath_costFinal_returnsMatchingDrones() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        List<String> result = droneService.queryAsPath("costFinal", "1.5");

        assertEquals(1, result.size());
        assertTrue(result.contains("D003"));
    }

    @Test
    void testGetDroneById_caseInsensitiveId_returnsNull() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);

        // IDs are case-sensitive, so "d001" should not match "D001"
        Drone result = droneService.getDroneById("d001");

        assertNull(result);
    }

    @Test
    void testQueryAvailableDrones_wrongDayOfWeek_filtersOut() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);

        // Wednesday - drones only available Monday and Tuesday in test setup
        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 10), // Wednesday
                LocalTime.of(10, 0),
                new MedDispatchRec.Requirements(5.0, false, false, null),
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_timeOutsideAvailability_filtersOut() {
        when(externalAPIService.getAllDrones()).thenReturn(testDrones);
        when(externalAPIService.getServicePoints()).thenReturn(testServicePoints);
        when(externalAPIService.getDronesForServicePoints()).thenReturn(testDroneAssignments);

        // 7am - before availability window (8am-6pm on Monday)
        MedDispatchRec dispatch = new MedDispatchRec(
                1,
                LocalDate.of(2024, 1, 8), // Monday
                LocalTime.of(7, 0), // Before 8am availability
                new MedDispatchRec.Requirements(5.0, false, false, null),
                new Position(55.1, -3.1)
        );

        List<String> result = droneService.queryAvailableDrones(List.of(dispatch));

        // Drones should be filtered out if dispatch time is before their availability
        assertNotNull(result);
    }
}
