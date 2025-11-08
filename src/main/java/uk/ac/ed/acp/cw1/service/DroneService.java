package uk.ac.ed.acp.cw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw1.dto.Drone;
import uk.ac.ed.acp.cw1.dto.QueryAttribute;

import javax.management.Attribute;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneService {

    private final ExternalAPIService externalAPIService;

    @Autowired
    public DroneService(ExternalAPIService externalAPIService){
        this.externalAPIService = externalAPIService;
    }


    /**
     * Fetches all drones from the {@link DroneService} and filters for cooling
     * @param state boolean of whether to return drones with cooling or without
     * @return list of drone id's that have cooling
     */
    public List<String> getDronesWithCooling(boolean state){
        //fetch all drones from the external service
        List<Drone> allDrones = externalAPIService.getAllDrones();
        //go through all drones
        return allDrones.stream()
                //filter for drones that have cooling attribute = true
                .filter(drone -> drone.getCapability().isCooling() == state)
                //for these drones, create a list containing their id's
                .map(drone -> String.valueOf(drone.getId()))
                .collect(Collectors.toList());

    }

    public Drone getDroneById(Integer id) {
        //go through all drones
        return externalAPIService.getAllDrones().stream()
                //filter for drones which id matches the given parameter
                .filter(drone -> drone.getId().equals(id))
                //get the first instance of a match
                .findFirst()
                //otherwise return null
                .orElse(null);
    }

    public List<String> queryAsPath(String attributeName, String attributeValue){
        //as "=" is passed as an operator, operator should never be null
        QueryAttribute queryAttribute = new QueryAttribute(attributeName, "=", attributeValue);
        return externalAPIService.getAllDrones().stream()
                .filter(drone -> AttributeComparator.matches(drone, queryAttribute))
                .map(drone -> String.valueOf(drone.getId()))
                .collect(Collectors.toList());

    }

    public List<String> query(List<QueryAttribute> queryAttributes){
        return externalAPIService.getAllDrones().stream()
                .filter(drone -> queryAttributes.stream()
                        .allMatch(queryAttribute -> AttributeComparator.matches(drone, queryAttribute)))
                .map(drone -> String.valueOf(drone.getId()))
                .collect(Collectors.toList());
    }

}
