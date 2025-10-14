package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.dto.Region;
import uk.ac.ed.acp.cw1.service.DistanceService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DistanceServiceTests {
    //the allowed difference in the distances accuracy, 5 digits of precision
    private DistanceService distanceService;

    //euclideanDistance tests
    private final double DISTANCE_DELTA = 0.00001;

    @BeforeEach
    public void setUp() {
        distanceService = new DistanceService();
    }
    @Test
    public void testEuclideanDistance_samePositions(){
        Position position1 = new Position(50.0,-30.0);
        Position position2 = new Position(50.0,-30.0);

        double distance = distanceService.euclideanDistance(position1, position2);
        assertEquals(0.0, distance, DISTANCE_DELTA);
    }

    @Test
    public void testEuclideanDistance_differentPositions(){
        Position position1 = new Position(50.0,0.0);
        Position position2 = new Position(50.0,100.0);

        double distance = distanceService.euclideanDistance(position1, position2);
        assertEquals(100.0, distance, DISTANCE_DELTA);
    }

    @Test
    public void testEuclideanDistance_similarPositions(){
        Position position1 = new Position(0.0,0.0001);
        Position position2 = new Position(-0.0001,0.0);

        double distance = distanceService.euclideanDistance(position1, position2);
        assertEquals(0.00014142, distance, DISTANCE_DELTA);
    }

    @Test
    public void testEuclideanDistance_symetry(){
        Position position1 = new Position(-67.349,7.830);
        Position position2 = new Position(36.435,-98.005);

        double distance1 =  distanceService.euclideanDistance(position1, position2);
        double distance2 =  distanceService.euclideanDistance(position2, position1);

        assertEquals(distance1, distance2, DISTANCE_DELTA);
    }

    @Test
    public void testEuclideanDistance_largelyDifferentPositions(){
        Position position1 = new Position(-176.349,169.676);
        Position position2 = new Position(-173.673,-179.673);

        double distance =  distanceService.euclideanDistance(position1, position2);

        assertEquals(349.359248, distance, DISTANCE_DELTA);
    }

    //isCloseTo tests
    double CLOSE_DISTANCE = 0.00015;
    @Test
    public void testIsCloseTo_samePosition(){
        Position position1 = new Position(50.0,-30.0);
        Position position2 = new Position(50.0,-30.0);

       assertTrue(distanceService.isCloseTo(position1, position2));
    }

    @Test
    public void testIsCloseTo_closePositions(){
        Position position1 = new Position(1.0000,-10.0000);
        Position position2 = new Position(1.0001,-10.0001);

        assertTrue(distanceService.isCloseTo(position1, position2));
    }

    @Test
    public void testIsCloseTo_onBoundary(){
        Position position1 = new Position(0.0, 0.0 + CLOSE_DISTANCE );
        Position position2 = new Position(0.0, 0.0);

        assertFalse(distanceService.isCloseTo(position1, position2));
    }

    @Test
    public void testIsCloseTo_justWithinBoundary(){
        Position position1 = new Position(0.0, 0.0 + CLOSE_DISTANCE - 0.000000001);
        Position position2 = new Position(0.0, 0.0);

        assertTrue(distanceService.isCloseTo(position1, position2));
    }

    @Test
    public void testIsCloseTo_farPositions(){
        Position position1 = new Position(-180.0,-180.0);
        Position position2 = new Position(180.0,180.0);

        assertFalse(distanceService.isCloseTo(position1, position2));
    }

    //nextPosition tests
    double STEP_SIZE = 0.00015;

    @Test
    public void testNextPosition_0Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 0.0;

        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        //latitude dosent change with 0.0 angle
        assertEquals(startPosition.getLat(), nextPosition.getLat(), DISTANCE_DELTA);
        //test the new next positions latitude is one step size larger than the start
        assertEquals(startPosition.getLng() + STEP_SIZE, nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);
    }

    @Test
    public void testNextPosition_90Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 90.0;

        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        //test the new next positions longitude is one step size larger than the start
        assertEquals(startPosition.getLat() + STEP_SIZE, nextPosition.getLat(), DISTANCE_DELTA);
        //longitude dosent change with 90.0 angle
        assertEquals(startPosition.getLng(), nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);

    }

    @Test
    public void testNextPosition_180Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 180.0;

        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        //latitude dosent change with 180.0 angle
        assertEquals(startPosition.getLat(), nextPosition.getLat(), DISTANCE_DELTA);
        //test the new next positions latitude is one step size smaller than the start
        assertEquals(startPosition.getLng() - STEP_SIZE, nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);

    }

    @Test
    public void testNextPosition_270Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 270.0;

        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        //test the new next positions latitude is one step size smaller than the start
        assertEquals(startPosition.getLat() - STEP_SIZE, nextPosition.getLat(), DISTANCE_DELTA);
        //longitude dosent change with 270.0 angle
        assertEquals(startPosition.getLng(), nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);

    }

    @Test
    public void testNextPosition_360Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 360.0;

        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        //latitude dosent change with 360.0 angle
        assertEquals(startPosition.getLat(), nextPosition.getLat(), DISTANCE_DELTA);
        //test the new next positions latitude is one step size larger than the start
        assertEquals(startPosition.getLng() + STEP_SIZE, nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);

    }

    @Test
    public void testNextPosition_45Angle(){
        Position startPosition = new Position(0.0, 0.0);
        double angle = 45.0;
        double expectedLat = 0.000106;
        double expectedLng = 0.000106;
        Position nextPosition = distanceService.nextPosition(startPosition, angle);
        assertEquals(expectedLat, nextPosition.getLat(), DISTANCE_DELTA);
        assertEquals(expectedLng, nextPosition.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition);
    }

    @Test
    public void testNextPosition_wrapAngle(){
        Position startPosition = new Position(0.0, 0.0);
        //655.5 - 360 = 295.5, should produce the same position
        double angle1 = 655.5;
        double angle2 = 295.5;
        Position nextPosition1 = distanceService.nextPosition(startPosition, angle1);
        Position nextPosition2 = distanceService.nextPosition(startPosition, angle2);
        assertEquals(nextPosition1.getLat(), nextPosition2.getLat(), DISTANCE_DELTA);
        assertEquals(nextPosition1.getLng(), nextPosition2.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition1);
        assertInstanceOf(Position.class, nextPosition2);


    }

    @Test
    public void testNextPosition_wrapNegativeAngle(){
        Position startPosition = new Position(0.0, 0.0);
        //295.5 - 360 = 64.5, should produce the same result
        double angle1 = 295.5;
        double angle2 = -64.5;
        Position nextPosition1 = distanceService.nextPosition(startPosition, angle1);
        Position nextPosition2 = distanceService.nextPosition(startPosition, angle2);
        assertEquals(nextPosition1.getLat(), nextPosition2.getLat(), DISTANCE_DELTA);
        assertEquals(nextPosition1.getLng(), nextPosition2.getLng(), DISTANCE_DELTA);
        assertInstanceOf(Position.class, nextPosition1);
        assertInstanceOf(Position.class, nextPosition2);
    }


    //isInRegion tests


    private Region createSimpleRegion() {
        List<Position> vertices = List.of(
                new Position(0.0, 1.0),
                new Position(1.0, 0.0),
                new Position(0.0, -1.0),
                new Position(-1.0, 0.0),
                new Position(0.0, 1.0)
        );
        return new Region("simple", vertices);
    }
    @Test
    public void testIsInRegion_pointInRegion(){
        Position position = new Position(0.0, 0.0);
        Region region = createSimpleRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_pointOnEdge(){
        Position position = new Position(1.0, 0.0);
        Region region = createSimpleRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_pointJustInside(){
        Position position = new Position( 0.0, 0.999999999999);
        Region region = createSimpleRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_pointJustOutside(){
        Position position = new Position( 0.0, 1.0000001);
        Region region = createSimpleRegion();
        assertFalse(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_pointFarOutside(){
        Position position = new Position(180.0, -180.0);
        Region region = createSimpleRegion();
        assertFalse(distanceService.isInRegion(position, region));

    }

    private Region createComplexRegion() {
        List<Position> vertices = List.of(
                new Position(0.0, 0.0),
                new Position(2.0, 1.0),
                new Position(3.0, 3.0),
                new Position(1.5, 4.0),
                new Position(0.0, 3.0),
                new Position(-1.0, 1.5),
                new Position(0.0, 0.0)
        );
        return new Region("complex", vertices);
    }



    @Test
    public void testIsInRegion_complexPointInRegion(){
        Position position = new Position(0.0, 1.0);
        Region region = createComplexRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_complexPointOnEdge(){
        Position position = new Position(3.0, 3.0);
        Region region = createComplexRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_complexPointJustInside(){
        Position position = new Position( 2.99999999999, 3.0);
        Region region = createComplexRegion();
        assertTrue(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_complexPointJustOutside(){
        Position position = new Position( 3.0000000001, 3.0);
        Region region = createComplexRegion();
        assertFalse(distanceService.isInRegion(position, region));
    }

    @Test
    public void testIsInRegion_complexPointFarOutside(){
        Position position = new Position(180.0, -180.0);
        Region region = createComplexRegion();
        assertFalse(distanceService.isInRegion(position, region));

    }



}
