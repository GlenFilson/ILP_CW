# CI Pipeline

## Pipeline Stages

| Stage | Actions | Output |
|-------|---------|--------|
| **Build & Test** | Compile, run tests, generate JaCoCo report | Test results, per-class coverage table in job summary |
| **Docker Build** | Package JAR, build Docker image | Verified container image with size |


## Quality Gates

| Gate | Threshold | Requirement |
|------|-----------|-------------|
| All tests pass | 100% | All requirements |
| Coverage visible | Per-class breakdown in summary | LO3 |
| Docker image builds | Success | S-02 |

## Artifacts

| Artifact | Contents | Retention |
|----------|----------|-----------|
| `coverage-report/` | Full JaCoCo HTML report | 30 days |
| `test-results/` | Surefire XML reports | 30 days |

## Code Review Checklist

### Functional Correctness
| Check | Requirement |
|-------|-------------|
| `isCloseTo` uses strictly-less-than (`<`) | U-01 |
| `isInRegion` handles edge and vertex cases | U-02 |
| `queryAvailableDrones` applies all constraints | I-01, I-02 |
| API failures return empty collections | I-03 |
| Paths respect no-fly zones and move limits | S-01 |

### Input Validation
| Check | Requirement |
|-------|-------------|
| `@Valid` on controller parameters | V-01 |
| `@ClosedPolygon` rejects open polygons | V-02 |

## Evaluation

**Strengths:**
- Automated testing on every push/pull request
- Coverage breakdown by package posted to summary
- Docker build verifies containerisation

**Limitations:**
- No live API integration tests (mitigated by mocking)
- No automated deployment