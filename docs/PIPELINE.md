# CI/CD Pipeline and Code Review

## Pipeline Overview

## Pipeline Stages

| Stage | Actions | Artifacts |
|-------|---------|-----------|
| **Build & Test** | Compile, run all unit/integration tests, generate JaCoCo report | `coverage-report/`, `test-results/` |
| **Docker Build** | Package JAR, build Docker image, verify image created | Docker image |

## Quality Gates

| Gate | Criteria | Requirement |
|------|----------|-------------|
| Tests pass | All unit and integration tests green | All requirements |
| Coverage generated | JaCoCo report uploaded as artifact | LO3 |
| Docker builds | Image builds successfully | S-02 |

## Code Review Checklist

### Functional Correctness
| Check | Requirement |
|-------|-------------|
| `isCloseTo` uses strictly-less-than (<) comparison | U-01 |
| `isInRegion` handles points on edges and vertices | U-02 |
| `queryAvailableDrones` filters by all constraints | I-01, I-02 |
| API failures caught and return empty collections | I-03 |
| Paths avoid no-fly zones and respect move limits | S-01 |

### Input Validation
| Check | Requirement |
|-------|-------------|
| `@Valid` annotation on controller request parameters | V-01 |
| `@ClosedPolygon` validator rejects open polygons | V-02 |
| Invalid JSON returns HTTP 400 | V-01 |

### Code Quality
| Check | Purpose |
|-------|---------|
| Constants used instead of magic numbers (e.g., `0.00015`) | Maintainability |
| Logging present in catch blocks | Debugging |
| No uncaught exceptions propagate to client | Robustness |

## Pipeline Evaluation

**Strengths:**
- Automated test execution on every push
- Coverage reports preserved for review
- Docker build verifies containerisation works

**Limitations:**
- No live API integration tests (mitigated by mocking in unit tests)
- No automated deployment