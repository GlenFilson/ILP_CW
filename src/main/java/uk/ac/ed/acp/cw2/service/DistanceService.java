package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.model.Position;

@Service
public class DistanceService {

    private final Double thresholdDistance = 0.00015;

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
        return distance < thresholdDistance;
    }
}
