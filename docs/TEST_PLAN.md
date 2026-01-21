# Test Plan

## Requirement Prioritisation
| Priority | Requirement | Reasoning |
|----------|-------------|-----------|
| **High** | S-01 (calcDeliveryPath) | Core business logic; failure means the system cannot fulfil its primary purpose. Complex interactions between pathfinding, drone constraints, and no-fly zones. |
| **High** | I-01, I-02 (drone availability) | Incorrect filtering could assign unavailable drones or reject valid ones. Directly impacts whether deliveries can be made. |
| **Medium** | U-01 (isCloseTo) | Used throughout pathfinding to determine delivery completion. Boundary errors would cause incorrect delivery detection. |
| **Medium** | U-02 (isInRegion) | Determines no-fly zone violations. Incorrect results could produce invalid paths. |
| **Medium** | I-03 (API failure handling) | External API is outside our control; graceful degradation is essential for robustness. |
| **Lower** | NF-01 (performance) | Important but unlikely to fail given simple operations; 30-second limit is generous. |
| **Lower** | V-01, V-02 (input validation) | Spring handles most JSON validation automatically; fewer custom validation paths to test. |
| **Lower** | NF-02 (liveness), S-02 (Docker) | Spring Actuator provides this out-of-box; minimal custom code involved. |


## Scaffolding
### Mock Objects
**ExternalAPIService Mock:**
To test `DroneService` and `PathfindingService` without calling the real ILP REST API, a mock must return controlled data:

```java
@MockBean
private ExternalAPIService externalAPIService;

when(externalAPIService.getAllDrones()).thenReturn(List.of(
    new Drone("1", "TestDrone", new Capability(10.0, true, false, 2000, 0.01, 1.0, 1.0))
));
when(externalAPIService.getServicePoints()).thenReturn(List.of(
    new ServicePoint("SP1", 1, new Position(55.944, -3.188))
));
```

**RestTemplate Mock (for I-03):**
To simulate external API failure:

```java
when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Drone[].class)))
    .thenThrow(new RestClientException("Connection refused"));
```

## Test Data
Pre-defined test fixtures representing:
- Valid dispatch records with varying requirements (cooling, heating, capacity)
- Drone configurations matching/not matching dispatch requirements
- Polygon regions for `isInRegion` testing (convex, concave, edge cases)
- Malformed JSON payloads for validation testing


## MockMvc Setup
Controller tests use MockMvc to simulate HTTP requests without starting a server:

```java
@WebMvcTest(DistanceController.class)
class DistanceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DistanceService distanceService;
}
```

## Instrumentation

### Code Coverage
**Configuration:**
Code coverage analysis has been configured using JaCoCo. 
JaCoCo will generate a report after each test run using `mvn test`

**Purpose:** 
Identifies untested code paths in service classes, particularly:
- Branches in `AttributeComparator.matches()` (multiple attribute types)
- Edge cases in `isInRegion()` (point on edge, at vertex)
- Error handling paths in `ExternalAPIService`

**Target:** >80% line coverage for service classes.

### Logging

`ExternalAPIService` includes logging for API call failures:

```java
catch(RestClientException e) {
    log.warn("Failed to fetch drones from external API: {}", e.getMessage());
    return new ArrayList<>();
}
```

This aids debugging when integration tests fail and provides evidence of graceful error handling.

### Timing Assertions
For NF-01, tests include timeout instrumentation:

```java
@Test
void calcDeliveryPath_completesWithinTimeLimit() {
    assertTimeout(Duration.ofSeconds(30), () -> {
        pathfindingService.calcDeliveryPath(dispatches);
    });
}
```

### Health Endpoint
Spring Actuator provides `/actuator/health` without custom instrumentation. This is used to verify NF-02 (liveness) after error scenarios.


## Evaluation of the Test Plan
### Strenghts
- Prioritisation focuses on testing high-risk components (pathfinding, drone filtering, region checking)
- Lifecycle placement ensures tests exist before dependent code is written
- Mocking strategy isolates components and allows for controlled testing

### Weaknsesses
- No tests against the live `ILP_API_ENDPOINT`, may miss response format differences or network issues
- Path optimality is not verified, though the severity is low as the pathfinder is not required to find the optimal path
- No load testing, may miss race conditions or code inefficieny, though the severity is low as the service is dockerised

### Plan Adequacy
The plan covers the core requirements. The highest-risk components receive the most attention with system-level tests verifying path validity. The main gap is the absence of live API testing, which is migitgated by manual smoke testing.

## Evaluation of Instrumentation
### Adequacy Assessment
| Instrumentation | Adequate? | Justification |
|-----------------|-----------|---------------|
| JaCoCo coverage | Yes | Identifies untested code; widely used industry standard |
| MockMvc | Yes | Tests full HTTP request/response cycle including validation |
| Mockito mocks | Yes | Enables isolation of components with external dependencies |
| Logging | Partial | Present in `ExternalAPIService` but sparse elsewhere |
| Timing assertions | Yes | Directly verifies NF-01 requirement |

### Potential Improvements

| Current State | Improvement | Benefit |
|---------------|-------------|---------|
| Line coverage only | Add branch coverage targets | Better assurance that conditional logic is tested |
| No profiling | Add timing instrumentation to `isInRegion` | Identify if point-in-polygon algorithm is a bottleneck |
| Manual coverage review | Add coverage thresholds to build | Fail build if coverage drops below target |

### Conclusion

The instrumentation is sufficient for the planned testing. JaCoCo provides visibility into test coverage, and the mocking infrastructure enables isolated, repeatable tests. The main improvement would be adding branch coverage targets and integrating coverage checks into the build process.
