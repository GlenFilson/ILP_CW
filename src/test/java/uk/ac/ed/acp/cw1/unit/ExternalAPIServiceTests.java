package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw1.dto.*;
import uk.ac.ed.acp.cw1.service.ExternalAPIService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalAPIService Unit Tests")
public class ExternalAPIServiceTests {

    @Mock
    private RestTemplate restTemplate;

    private ExternalAPIService externalAPIService;

    private static final String TEST_ENDPOINT = "https://test-ilp-api.example.com";

    @BeforeEach
    void setUp() {
        externalAPIService = new ExternalAPIService(restTemplate, TEST_ENDPOINT);
    }

    // HELPER METHODS

    private Drone createTestDrone(String id, String name, double capacity) {
        Drone drone = new Drone();
        drone.setId(id);
        drone.setName(name);
        Drone.Capability cap = new Drone.Capability();
        cap.setCapacity(capacity);
        cap.setMaxMoves(1000);
        cap.setCooling(true);
        cap.setHeating(false);
        cap.setCostPerMove(0.1);
        cap.setCostInitial(1.0);
        cap.setCostFinal(1.0);
        drone.setCapability(cap);
        return drone;
    }

    private ServicePoint createTestServicePoint(String name, int id, double lat, double lng) {
        return new ServicePoint(name, id, new Position(lat, lng));
    }

    private RestrictedArea createTestRestrictedArea(String name) {
        RestrictedArea area = new RestrictedArea();
        area.setName(name);
        area.setVertices(List.of(
                new Position(55.0, -3.0),
                new Position(55.0, -3.1),
                new Position(55.1, -3.1),
                new Position(55.1, -3.0),
                new Position(55.0, -3.0)
        ));
        return area;
    }

    private DroneForServicePointResponse createTestDroneAssignment(int servicePointId, String droneId) {
        DroneForServicePointResponse response = new DroneForServicePointResponse();
        response.setServicePointId(servicePointId);
        DroneAvailability avail = new DroneAvailability();
        avail.setId(droneId);
        avail.setAvailability(List.of(
                new Availability(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))
        ));
        response.setDrones(List.of(avail));
        return response;
    }


    @Test
    @DisplayName("getAllDrones: Successfully fetches drones from API")
    void testGetAllDrones_success_returnsDroneList() {
        Drone[] drones = {
                createTestDrone("D001", "DroneAlpha", 10.0),
                createTestDrone("D002", "DroneBeta", 15.0)
        };
        ResponseEntity<Drone[]> responseEntity = new ResponseEntity<>(drones, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenReturn(responseEntity);

        List<Drone> result = externalAPIService.getAllDrones();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("D001", result.get(0).getId());
        assertEquals("D002", result.get(1).getId());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(), eq(Drone[].class));
    }

    @Test
    @DisplayName("getAllDrones: Returns empty list when response body is null")
    void testGetAllDrones_nullBody_returnsEmptyList() {
        ResponseEntity<Drone[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenReturn(responseEntity);

        List<Drone> result = externalAPIService.getAllDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllDrones: Returns empty list on RestClientException")
    void testGetAllDrones_restClientException_returnsEmptyList() {
        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenThrow(new RestClientException("Connection timeout"));

        List<Drone> result = externalAPIService.getAllDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllDrones: Returns empty list on server error (5xx)")
    void testGetAllDrones_serverError_returnsEmptyList() {
        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenThrow(new RestClientException("500 Internal Server Error"));

        List<Drone> result = externalAPIService.getAllDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllDrones: Returns empty array as empty list")
    void testGetAllDrones_emptyArray_returnsEmptyList() {
        Drone[] drones = {};
        ResponseEntity<Drone[]> responseEntity = new ResponseEntity<>(drones, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenReturn(responseEntity);

        List<Drone> result = externalAPIService.getAllDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getServicePoints: Successfully fetches service points from API")
    void testGetServicePoints_success_returnsServicePointList() {
        ServicePoint[] servicePoints = {
                createTestServicePoint("SP1", 1, 55.944, -3.188),
                createTestServicePoint("SP2", 2, 55.945, -3.189)
        };
        ResponseEntity<ServicePoint[]> responseEntity = new ResponseEntity<>(servicePoints, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        )).thenReturn(responseEntity);

        List<ServicePoint> result = externalAPIService.getServicePoints();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    @DisplayName("getServicePoints: Returns empty list when response body is null")
    void testGetServicePoints_nullBody_returnsEmptyList() {
        ResponseEntity<ServicePoint[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        )).thenReturn(responseEntity);

        List<ServicePoint> result = externalAPIService.getServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getServicePoints: Returns empty list on API error")
    void testGetServicePoints_apiError_returnsEmptyList() {
        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        )).thenThrow(new RestClientException("Service unavailable"));

        List<ServicePoint> result = externalAPIService.getServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getServicePoints: Returns empty array as empty list")
    void testGetServicePoints_emptyArray_returnsEmptyList() {
        ServicePoint[] servicePoints = {};
        ResponseEntity<ServicePoint[]> responseEntity = new ResponseEntity<>(servicePoints, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        )).thenReturn(responseEntity);

        List<ServicePoint> result = externalAPIService.getServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getRestrictedAreas: Successfully fetches restricted areas from API")
    void testGetRestrictedAreas_success_returnsRestrictedAreaList() {
        RestrictedArea[] areas = {
                createTestRestrictedArea("Zone1"),
                createTestRestrictedArea("Zone2")
        };
        ResponseEntity<RestrictedArea[]> responseEntity = new ResponseEntity<>(areas, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/restricted-areas"),
                eq(HttpMethod.GET),
                isNull(),
                eq(RestrictedArea[].class)
        )).thenReturn(responseEntity);

        List<RestrictedArea> result = externalAPIService.getRestrictedAreas();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Zone1", result.get(0).getName());
        assertEquals("Zone2", result.get(1).getName());
    }

    @Test
    @DisplayName("getRestrictedAreas: Returns empty list when response body is null")
    void testGetRestrictedAreas_nullBody_returnsEmptyList() {
        ResponseEntity<RestrictedArea[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/restricted-areas"),
                eq(HttpMethod.GET),
                isNull(),
                eq(RestrictedArea[].class)
        )).thenReturn(responseEntity);

        List<RestrictedArea> result = externalAPIService.getRestrictedAreas();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getRestrictedAreas: Returns empty list on API error")
    void testGetRestrictedAreas_apiError_returnsEmptyList() {
        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/restricted-areas"),
                eq(HttpMethod.GET),
                isNull(),
                eq(RestrictedArea[].class)
        )).thenThrow(new RestClientException("Network error"));

        List<RestrictedArea> result = externalAPIService.getRestrictedAreas();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getRestrictedAreas: No zones returns empty list")
    void testGetRestrictedAreas_noZones_returnsEmptyList() {
        RestrictedArea[] areas = {};
        ResponseEntity<RestrictedArea[]> responseEntity = new ResponseEntity<>(areas, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/restricted-areas"),
                eq(HttpMethod.GET),
                isNull(),
                eq(RestrictedArea[].class)
        )).thenReturn(responseEntity);

        List<RestrictedArea> result = externalAPIService.getRestrictedAreas();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getDronesForServicePoints: Successfully fetches assignments from API")
    void testGetDronesForServicePoints_success_returnsAssignmentList() {
        DroneForServicePointResponse[] assignments = {
                createTestDroneAssignment(1, "D001"),
                createTestDroneAssignment(2, "D002")
        };
        ResponseEntity<DroneForServicePointResponse[]> responseEntity =
                new ResponseEntity<>(assignments, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones-for-service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(DroneForServicePointResponse[].class)
        )).thenReturn(responseEntity);

        List<DroneForServicePointResponse> result = externalAPIService.getDronesForServicePoints();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getServicePointId());
        assertEquals(2, result.get(1).getServicePointId());
    }

    @Test
    @DisplayName("getDronesForServicePoints: Returns empty list when response body is null")
    void testGetDronesForServicePoints_nullBody_returnsEmptyList() {
        ResponseEntity<DroneForServicePointResponse[]> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones-for-service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(DroneForServicePointResponse[].class)
        )).thenReturn(responseEntity);

        List<DroneForServicePointResponse> result = externalAPIService.getDronesForServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getDronesForServicePoints: Returns empty list on API error")
    void testGetDronesForServicePoints_apiError_returnsEmptyList() {
        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones-for-service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(DroneForServicePointResponse[].class)
        )).thenThrow(new RestClientException("Timeout"));

        List<DroneForServicePointResponse> result = externalAPIService.getDronesForServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getDronesForServicePoints: Empty array returns empty list")
    void testGetDronesForServicePoints_emptyArray_returnsEmptyList() {
        DroneForServicePointResponse[] assignments = {};
        ResponseEntity<DroneForServicePointResponse[]> responseEntity =
                new ResponseEntity<>(assignments, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones-for-service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(DroneForServicePointResponse[].class)
        )).thenReturn(responseEntity);

        List<DroneForServicePointResponse> result = externalAPIService.getDronesForServicePoints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getAllDrones: Uses correct endpoint URL")
    void testGetAllDrones_correctEndpointUrl() {
        Drone[] drones = {};
        ResponseEntity<Drone[]> responseEntity = new ResponseEntity<>(drones, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        )).thenReturn(responseEntity);

        externalAPIService.getAllDrones();

        verify(restTemplate).exchange(
                eq(TEST_ENDPOINT + "/drones"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Drone[].class)
        );
    }

    @Test
    @DisplayName("getServicePoints: Uses correct endpoint URL")
    void testGetServicePoints_correctEndpointUrl() {
        ServicePoint[] servicePoints = {};
        ResponseEntity<ServicePoint[]> responseEntity = new ResponseEntity<>(servicePoints, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        )).thenReturn(responseEntity);

        externalAPIService.getServicePoints();

        verify(restTemplate).exchange(
                eq(TEST_ENDPOINT + "/service-points"),
                eq(HttpMethod.GET),
                isNull(),
                eq(ServicePoint[].class)
        );
    }
}
