package uk.ac.ed.acp.cw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw1.dto.Drone;
import uk.ac.ed.acp.cw1.dto.MedDispatchRec;
import uk.ac.ed.acp.cw1.dto.QueryAttribute;

import javax.management.Attribute;
import java.util.List;
import java.util.Objects;
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

    public List<String> queryAvailableDrones(List<MedDispatchRec> dispatches) {
        List<Drone> allDrones = externalAPIService.getAllDrones();
        //get all drones
        //given a list of dispatches, return drones which fulfill all of them
        //need to check the total capacity needed for the dispatches
        //need to check if any dispatches require cooling or heating
        //if maxCost is given, need to find the minimum maxCost (most restrictive) and ensure drone meets


        //sums the capacity of all dispatches
        double totalCapacity = dispatches.stream()
                .mapToDouble(dispatch -> dispatch.getRequirements().getCapacity())
                .sum();
        boolean coolingRequired = dispatches.stream()
                .anyMatch(dispatch -> dispatch.getRequirements().isCooling());

        boolean heatingRequired = dispatches.stream()
                .anyMatch(dispatch -> dispatch.getRequirements().isHeating());

        //we want to find the most restrictive maxCost constraint, the minimum maxCost in the dispatches
        Double minimumMaxCost = dispatches.stream()
                .map(dispatch -> dispatch.getRequirements().getMaxCost())
                //maxCost is optional so handle null values, ignore them
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);


        //uses helper function canFulfillAllDispatches to compare all drones to the calculated constraints from dispatches
        return allDrones.stream()
                .filter(drone -> canFulfillAllDispatches(drone, totalCapacity, coolingRequired, heatingRequired, minimumMaxCost))
                .map(drone -> String.valueOf(drone.getId()))
                .collect(Collectors.toList());
    }

    private boolean canFulfillAllDispatches(Drone drone,
                                            double totalCapacityNeeded,
                                            boolean coolingRequired,
                                            boolean heatingRequired,
                                            Double maxCostAllowed) {
        Drone.Capability capability = drone.getCapability();
        //check capacity constraint
        if(capability.getCapacity() < totalCapacityNeeded){
            return false;
        }
        //if the drone dosent have cooling, and cooling is required return false
        if(!capability.isCooling() && coolingRequired ){
            return false;
        }

        //if the drone dosent have heating, and heating is required return false
        if(!capability.isHeating() && heatingRequired ){
            return false;
        }

        if(maxCostAllowed != null){
            //calculate the total cost
            double droneTotalCost = capability.getCostInitial() + capability.getCostFinal();
            //if the drone will cost more than allowed, return false
            if (droneTotalCost > maxCostAllowed){
                return false;
            }
        }

        //if the drone passed all these constraints then return true
        return true;
        
    }

}
