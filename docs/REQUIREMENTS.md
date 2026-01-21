# Requirements

## Unit-Level Requirements
| ID | Requirement | Service | Method |
|----|-------------|---------|--------|
| U-01 | The service shall return `true` if two positions are within 0.00015 degrees (strictly less than), `false` otherwise | `DistanceService` | `isCloseTo()` |
| U-02 | The service shall correctly determine if a point lies inside, on the boundary of, or outside a closed polygon region | `DistanceService` | `isInRegion()` |

Test approach: These are functions with no external dependencies. They can be tested with boundary value analysis (U-01 at exactly 0.00015, just below and just above) and equivalance partitioning (U-02 points inside, outside, on edge, and at vertex)

## Integration-Level Requirements
| ID | Requirement | Service | Dependencies |
|----|-------------|---------|--------------|
| I-01 | The service shall return drone IDs that satisfy all dispatch requirements (capacity, cooling/heating, date/time availability) when queried | `DroneService` | `ExternalAPIService` |
| I-02 | The service shall return an empty list when no drone can satisfy all dispatch requirements | `DroneService` | `ExternalAPIService` |
| I-03 | The service shall handle external API unavailability gracefully without crashing, returning empty collections | `ExternalAPIService` | External ILP REST API |

Test approach: Mock `ExternalAPIService` with Mockito to control drone / service-point data. For I-03, mock `RestTemplate` to throw `RestClientException` and verify graceful handling.

### System-Level Requirements
| ID | Requirement | Endpoint |
|----|-------------|----------|
| S-01 | The service shall compute delivery paths that start and return to a service point, avoid no-fly zones, and respect drone move limits | `POST /api/v1/calcDeliveryPath` |
| S-02 | The service shall run inside a Docker container, listening on port 8080, and respond to health checks | `GET /actuator/health` |

Test approach: S-01 requires system tests with realistic dispatch data, verifying path validity (no no-fly zone violations, move count is within limits). S-02 is verified via container smoke test.

## Non-Functional Requirements
| ID | Requirement | Type | Measurement |
|----|-------------|------|-------------|
| NF-01 | All endpoints shall respond within the execution time limit (< 30 seconds) | Performance | Timeout assertions in tests |
| NF-02 | The service shall always return to a recognised "ready" state after processing any request (liveness property) | Liveness | Health endpoint returns `{"status": "UP"}` after all operations |

Test approach: NF-01 via timing assertions. NF-02 via health endpoint checks after error conditions and heavy load, to verify the service remains responsive.

## Input Validation Requirements
| ID | Requirement | Applies To |
|----|-------------|------------|
| V-01 | Malformed or syntactically invalid JSON shall result in HTTP 400 Bad Request | All POST endpoints |
| V-02 | Polygon regions where first and last vertices do not match (open polygons) shall be rejected with HTTP 400 | `POST /api/v1/isInRegion` |

Test approach: Negative testing with invalid JSON payloads and edge cases.
