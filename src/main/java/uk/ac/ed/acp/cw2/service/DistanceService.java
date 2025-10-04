package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.model.Position;
import uk.ac.ed.acp.cw2.model.Region;

import java.util.List;

@Service
public class DistanceService {

    private final Double THRESHOLD_DISTANCE = 0.00015;

    //allowed angle degrees
    private final double VALID_ANGLE_MULTIPLE = 22.5;

    //step size to move 
    private final double STEP_SIZE = 0.00015;

    /**
     * calculates the distance between 2 points using the Euclidean distance formula
     * @param position1 the first point
     * @param position2 the second point
     * @return the distance between the points
     */
    public Double euclideanDistance(Position position1, Position position2){
        double lat1 = position1.getLat();
        double lng1 = position1.getLng();

        double lat2 = position2.getLat();
        double lng2 = position2.getLng();

        //sqrt((x2-x1)^2 + (y2-y1))^2)
        return Math.sqrt(Math.pow(lat2-lat1, 2) + Math.pow(lng2-lng1, 2));
    }

    public Boolean isCloseTo(Position position1, Position position2){
        double distance = euclideanDistance(position1, position2);
        return distance < THRESHOLD_DISTANCE;
    }


    /**
     * relies on the STEP_SIZE to determine what distance exactly the next position should be
     * @param position the starting position
     * @param angle the angle in which the nextPosition should head
     * @return the next position, essentially moving the drone
     */
    public Position nextPosition(Position position, Double angle){
        //if the angle is not valid, is not a multiple of our specified value, return null
        if (angle % VALID_ANGLE_MULTIPLE != 0){
            return null;
        }
        double lat = position.getLat();
        double lng = position.getLng();

        double radians = Math.toRadians(angle);
        double deltaLat = STEP_SIZE * Math.sin(radians);
        double deltaLng = STEP_SIZE * Math.cos(radians);
        
        Position nextPosition = new Position();
        nextPosition.setLat(lat+deltaLat);
        nextPosition.setLng(lng+deltaLng);

        return nextPosition;
    }

    /**
     * Note: does count points on the edge as "inRegion" as Piazza comment:
     * "A point is inside if it’s strictly inside. You do not need to add an extra 0.00015 tolerance around the polygon edges. The automarker will expect the standard geometric definition here."
     * @param position the point to check
     * @param region a list of positions/points that form a polygon
     * @return whether the position is inside the polygon created by the list of positions/points, the region
     */
    public Boolean isInRegion(Position position, Region region){
        int n = region.getVertices().size();//number of vertices, point
        int intersections = 0;
        List<Position> vertices = region.getVertices();
        //x point, y point
        double xp  = position.getLat();
        double yp = position.getLng();;


        for (int i = 0, j = n - 1; i < n; j = i++){
            double x1 = vertices.get(i).getLat();
            double y1 = vertices.get(i).getLng();

            double x2 = vertices.get(j).getLat();
            double y2 = vertices.get(j).getLng();

            //if the points y coord is not above both or below both points, aka the points y coord is between the points y coords
            //does the ray cross the points in the y coords
            if ((y1 > yp) != (y2 > yp)) {
                //if crosses y coords then there is a possibility the ray also crosses x coords
                //find the x intersect
                //x = m*y + c
                double xIntersection = (x2 - x1) / (y2 - y1) * (yp - y1) + x1;
                if (xp < xIntersection) {
                    intersections++;
                }
            }
        }
        //if there are an odd number of intersections then the point is within the region
        return (intersections % 2 == 1);
    }
}
