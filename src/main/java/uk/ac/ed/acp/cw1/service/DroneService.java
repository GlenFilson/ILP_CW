package uk.ac.ed.acp.cw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw1.dto.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DroneService {

    private final ExternalAPIService externalAPIService;
    private final DistanceService distanceService;
    private static final double MOVE_DISTANCE = 0.00015; // Step size for drone movement

    @Autowired
    public DroneService(ExternalAPIService externalAPIService, DistanceService distanceService){
        this.externalAPIService = externalAPIService;
        this.distanceService = distanceService;
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

    public Drone getDroneById(String id) {
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
        if (dispatches == null || dispatches.isEmpty()) {
            return new ArrayList<>();
        }

        // NOTE: per forum/instructor clarification, queryAvailableDrones should return a
        // drone if it can satisfy ALL dispatch records (joined by AND), even when
        // the dispatches are on different dates. Availability and capability are
        // checked per-dispatch further down. Therefore we do not reject multi-day
        // inputs here.

        List<Drone> allDrones = externalAPIService.getAllDrones();
        List<DroneForServicePointResponse> dronesForServicePoints = externalAPIService.getDronesForServicePoints();
        List<ServicePoint> servicePoints = externalAPIService.getServicePoints();

        Map<String, DroneAvailability> droneAvailabilityMap = buildDroneAvailabilityMap(dronesForServicePoints);

        Map<String, Integer> droneToServicePointMap = buildDroneToServicePointMap(dronesForServicePoints);

        //get all drones
        //given a list of dispatches, return drones which fulfill all of them
        //need to check the maximum capacity needed for any single dispatch
        //need to check if any dispatches require cooling or heating
        //if maxCost is given, need to find the minimum maxCost (most restrictive) and ensure drone meets
        //check day and time availability for ALL dispatches

        //find the maximum capacity among all dispatches (drone makes separate trips)
        double maxCapacity = dispatches.stream()
                .mapToDouble(dispatch -> dispatch.getRequirements().getCapacity())
                .max()
                .orElse(0.0);
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
        //also checks availability for EVERY dispatch time (not just the first)
        return allDrones.stream()
                .filter(drone -> {
                    // determine assigned service point for this drone (if any)
                    Integer assignedSpId = droneToServicePointMap.get(drone.getId());
                    ServicePoint assignedSp = null;
                    if (assignedSpId != null) {
                        final Integer spIdFinal = assignedSpId;
                        assignedSp = servicePoints.stream()
                                .filter(sp -> sp.getId().equals(spIdFinal))
                                .findFirst()
                                .orElse(null);
                    }

                    // check capability with dispatches and service points for proper cost estimation
                    if (!canFulfillAllDispatches(drone, maxCapacity, coolingRequired, heatingRequired,
                                                  minimumMaxCost, dispatches, servicePoints, assignedSp)) {
                        return false;
                    }

                    // check availability for EVERY dispatch time
                    DroneAvailability availability = droneAvailabilityMap.get(drone.getId());
                    if (availability == null) {
                        return false; // No availability data = unavailable
                    }

                    // drone must be available for ALL dispatches
                    for (MedDispatchRec dispatch : dispatches) {
                        LocalDate date = dispatch.getDate();
                        LocalTime time = dispatch.getTime();
                        DayOfWeek dayOfWeek = date.getDayOfWeek();

                        if (!isDroneAvailable(availability, dayOfWeek, time)) {
                            return false; // Unavailable for this dispatch = reject drone
                        }
                    }

                    return true; // available for all dispatches
                })
                .map(drone -> String.valueOf(drone.getId()))
                .collect(Collectors.toList());
    }

    private Map<String, DroneAvailability> buildDroneAvailabilityMap(List<DroneForServicePointResponse> dronesForSP) {
        Map<String, DroneAvailability> map = new HashMap<>();

        for (DroneForServicePointResponse sp : dronesForSP) {
            for (DroneAvailability droneAvail : sp.getDrones()) {
                map.put(droneAvail.getId(), droneAvail);
            }
        }

        return map;
    }

    private Map<String, Integer> buildDroneToServicePointMap(List<DroneForServicePointResponse> dronesForSP) {
        Map<String, Integer> map = new HashMap<>();
        for (DroneForServicePointResponse sp : dronesForSP) {
            Integer spId = sp.getServicePointId();
            for (DroneAvailability droneAvail : sp.getDrones()) {
                map.put(droneAvail.getId(), spId);
            }
        }
        return map;
    }

    private boolean isDroneAvailable(DroneAvailability droneAvail, DayOfWeek dayOfWeek, LocalTime time) {
        for (Availability slot : droneAvail.getAvailability()) {
            if (slot.getDayOfWeek() == dayOfWeek) {
                LocalTime from = slot.getFrom();
                LocalTime until = slot.getUntil();

                // check if time falls within [from, until)
                if (!time.isBefore(from) && !time.isAfter(until)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canFulfillAllDispatches(Drone drone,
                                            double maxCapacityNeeded,
                                            boolean coolingRequired,
                                            boolean heatingRequired,
                                            Double maxCostAllowed,
                                            List<MedDispatchRec> dispatches,
                                            List<ServicePoint> servicePoints,
                                            ServicePoint assignedServicePoint) {
        Drone.Capability capability = drone.getCapability();

        //check capacity constraint (drone only needs to satisfy max single dispatch capacity)
        if(capability.getCapacity() < maxCapacityNeeded){
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

        //check maxMoves constraint for each dispatch (round trip)
        if(capability.getMaxMoves() != null && !dispatches.isEmpty()){
            for (MedDispatchRec dispatch : dispatches) {
                // use assigned service point for this drone if available, otherwise pick closest
                ServicePoint spToUse = assignedServicePoint != null ? assignedServicePoint : findClosestServicePoint(
                    dispatch.getDelivery(), servicePoints
                );

                // calculate distance from service point to delivery location (one way)
                double distance = distanceService.euclideanDistance(
                    spToUse.getLocation(),
                    dispatch.getDelivery()
                );

                // estimate moves needed for round trip
                double estimatedMovesOneWay = distance / MOVE_DISTANCE;
                double estimatedMovesRoundTrip = estimatedMovesOneWay * 2;

                // check if drone can complete this dispatch within maxMoves
                if(estimatedMovesRoundTrip > capability.getMaxMoves()){
                    return false;
                }
            }
        }

        //PIAZZA: Only perform attribute-based filtering and use a lower-bound or estimated cost such as
        //(distance(servicePoint, delivery)/step) × costPerMove + costInitial + costFinal, divided by the number of dispatches
        if(maxCostAllowed != null && !dispatches.isEmpty()){
            int numDispatches = dispatches.size();

            // calculate estimated cost per dispatch
            double totalEstimatedCost = 0.0;

            for (MedDispatchRec dispatch : dispatches) {
                // use assigned service point for this drone if available, otherwise pick closest
                ServicePoint spToUse = assignedServicePoint != null ? assignedServicePoint : findClosestServicePoint(
                    dispatch.getDelivery(), servicePoints
                );

                // calculate distance from service point to delivery location
                double distance = distanceService.euclideanDistance(
                    spToUse.getLocation(),
                    dispatch.getDelivery()
                );


                double estimatedMoves = (distance / MOVE_DISTANCE) * 2;

                // calculate cost for this dispatch
                double moveCost = estimatedMoves * capability.getCostPerMove();
                totalEstimatedCost += moveCost;
            }

            // add initial and final costs (pro rata)
            double fixedCostsPerDispatch = (capability.getCostInitial() + capability.getCostFinal()) / numDispatches;
            double totalMoveCostPerDispatch = totalEstimatedCost / numDispatches;
            double estimatedCostPerDispatch = fixedCostsPerDispatch + totalMoveCostPerDispatch;

            // check against the most restrictive maxCost
            return estimatedCostPerDispatch <= maxCostAllowed;
        }

        //if the drone passed all these constraints then return true
        return true;
    }

    /**
     * find the closest service point to a given delivery location
     * @param deliveryLocation the delivery destination
     * @param servicePoints list of available service points
     * @return the closest service point
     */
    private ServicePoint findClosestServicePoint(Position deliveryLocation, List<ServicePoint> servicePoints) {
        return servicePoints.stream()
                .min(Comparator.comparingDouble(sp ->
                    distanceService.euclideanDistance(sp.getLocation(), deliveryLocation)
                ))
                .orElse(servicePoints.get(0)); // Fallback to first service point
    }


}
