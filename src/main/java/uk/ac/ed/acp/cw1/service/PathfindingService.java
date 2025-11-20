package uk.ac.ed.acp.cw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw1.dto.*;

import java.util.*;

@Service
public class PathfindingService {

    private final DistanceService distanceService;
    private final ExternalAPIService externalAPIService;
    private final DroneService droneService;

    public PathfindingService(DistanceService distanceService, ExternalAPIService externalAPIService, DroneService droneService) {
        this.distanceService = distanceService;
        this.externalAPIService = externalAPIService;
        this.droneService = droneService;
    }
    private static final double[] VALID_ANGLES = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    public CalcDeliveryPathResponse calcDeliveryPath(List<MedDispatchRec> dispatches){
       if (dispatches == null || dispatches.isEmpty()){
           return new CalcDeliveryPathResponse(0.0, 0, new ArrayList<>());
       }

        //fetch all necessary data from external service
        List<Drone> drones = externalAPIService.getAllDrones();
        List<ServicePoint> servicePoints = externalAPIService.getServicePoints();
        List<DroneForServicePointResponse> dronesForServicePoints = externalAPIService.getDronesForServicePoints();
        List<RestrictedArea> restrictedAreas = externalAPIService.getRestrictedAreas();

        // Step 1: Group dispatches by date (treat null as same date)
        Map<java.time.LocalDate, List<MedDispatchRec>> dispatchesByDate = groupDispatchesByDate(dispatches);

        // Track results across all date groups
        List<DronePath> allDronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMoves = 0;

        // Step 2: Process each date group
        for (Map.Entry<java.time.LocalDate, List<MedDispatchRec>> entry : dispatchesByDate.entrySet()) {
            java.time.LocalDate date = entry.getKey();
            List<MedDispatchRec> dateDispatches = entry.getValue();

            System.out.println("DEBUG: Processing date group: " + date + " with " + dateDispatches.size() + " dispatches");

            // Step 3: For this date, filter drones that are available
            List<Drone> availableDrones = filterAvailableDrones(drones, date, dateDispatches, dronesForServicePoints);

            System.out.println("DEBUG: Found " + availableDrones.size() + " available drones for date " + date);

            // Track which dispatches have been assigned for this date
            Set<Integer> assignedDispatchIds = new HashSet<>();

            // Step 4: Apply pathfinding for each available drone
            for (Drone drone : availableDrones) {
                if (assignedDispatchIds.size() == dateDispatches.size()) {
                    break; // All dispatches for this date are assigned
                }

                // Get the service point for this drone
                ServicePoint servicePoint = getServicePointForDrone(drone.getId(), dronesForServicePoints, servicePoints);
                if (servicePoint == null) {
                    continue;
                }

                // Find unassigned dispatches this drone can handle
                List<MedDispatchRec> candidates = new ArrayList<>();
                for (MedDispatchRec dispatch : dateDispatches) {
                    if (assignedDispatchIds.contains(dispatch.getId())) {
                        continue;
                    }

                    // Check basic capability requirements
                    if (!canDeliverSingleDispatch(drone, dispatch)) {
                        continue;
                    }

                    candidates.add(dispatch);
                }

                if (candidates.isEmpty()) {
                    continue;
                }

                // Build optimal multi-delivery route for this drone
                List<MedDispatchRec> route = buildOptimalRoute(drone, servicePoint, candidates, restrictedAreas);

                if (route.isEmpty()) {
                    continue;
                }

                // Calculate the actual path for this route (expensive operation)
                DronePath dronePath = calculatePath(drone, servicePoint, route, restrictedAreas);

                // Validate the path against all constraints
                int pathMoves = countMoves(dronePath);

                // Check maxMoves constraint FIRST (cheaper than cost calculation)
                if (pathMoves > drone.getCapability().getMaxMoves()) {
                    continue;
                }

                // Only check cost if needed
                boolean hasCostConstraint = route.stream()
                        .anyMatch(d -> d.getRequirements().getMaxCost() != null);

                if (hasCostConstraint) {
                    double pathCost = drone.getCapability().getCostInitial()
                            + (pathMoves * drone.getCapability().getCostPerMove())
                            + drone.getCapability().getCostFinal();

                    double totalMaxCostAllowed = route.stream()
                            .filter(d -> d.getRequirements().getMaxCost() != null)
                            .mapToDouble(d -> d.getRequirements().getMaxCost())
                            .sum();

                    if (pathCost > totalMaxCostAllowed) {
                        continue;
                    }

                    totalCost += pathCost;
                } else {
                    // Calculate cost for response
                    double pathCost = drone.getCapability().getCostInitial()
                            + (pathMoves * drone.getCapability().getCostPerMove())
                            + drone.getCapability().getCostFinal();
                    totalCost += pathCost;
                }

                // Path is valid - add it to results
                allDronePaths.add(dronePath);
                totalMoves += pathMoves;

                // Mark these dispatches as assigned
                route.forEach(d -> assignedDispatchIds.add(d.getId()));
            }

            System.out.println("DEBUG: Completed date " + date + ": assigned " + assignedDispatchIds.size() + "/" + dateDispatches.size() + " dispatches");
        }

        return new CalcDeliveryPathResponse(totalCost, totalMoves, allDronePaths);
    }

    /**
     * Groups dispatches by their date. Treats null dates as the same date.
     */
    private Map<java.time.LocalDate, List<MedDispatchRec>> groupDispatchesByDate(List<MedDispatchRec> dispatches) {
        Map<java.time.LocalDate, List<MedDispatchRec>> grouped = new LinkedHashMap<>();

        for (MedDispatchRec dispatch : dispatches) {
            java.time.LocalDate date = dispatch.getDate(); // null dates will be treated as same key
            grouped.computeIfAbsent(date, k -> new ArrayList<>()).add(dispatch);
        }

        return grouped;
    }

    /**
     * Filters drones that are available for the given date and can deliver before the required times.
     * A drone is available if it has availability on that day of week and its time window starts before
     * the earliest dispatch time needed.
     */
    private List<Drone> filterAvailableDrones(List<Drone> allDrones,
                                               java.time.LocalDate date,
                                               List<MedDispatchRec> dateDispatches,
                                               List<DroneForServicePointResponse> assignments) {
        List<Drone> available = new ArrayList<>();

        // If date is null, all drones are potentially available
        if (date == null) {
            return new ArrayList<>(allDrones);
        }

        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();

        for (Drone drone : allDrones) {
            if (isDroneAvailableForDateGroup(drone, dayOfWeek, dateDispatches, assignments)) {
                available.add(drone);
            }
        }

        return available;
    }

    /**
     * Checks if a drone is available for a date group.
     * The drone must have availability on the day of week and its availability window
     * must start before all dispatch times in the group.
     */
    private boolean isDroneAvailableForDateGroup(Drone drone,
                                                   java.time.DayOfWeek dayOfWeek,
                                                   List<MedDispatchRec> dateDispatches,
                                                   List<DroneForServicePointResponse> assignments) {
        // Find this drone's availability schedule
        for (DroneForServicePointResponse assignment : assignments) {
            for (DroneAvailability availability : assignment.getDrones()) {
                if (availability.getId().equals(drone.getId())) {
                    // Check if drone has any availability slots for this day of week
                    for (Availability slot : availability.getAvailability()) {
                        if (slot.getDayOfWeek() == dayOfWeek) {
                            // Drone has availability on this day
                            // Check if it can deliver before all required times
                            java.time.LocalTime droneAvailableFrom = slot.getFrom();

                            // Check if drone is available before all dispatch times
                            boolean canDeliverAll = true;
                            for (MedDispatchRec dispatch : dateDispatches) {
                                if (dispatch.getTime() != null) {
                                    // Drone must be available before the dispatch time
                                    if (droneAvailableFrom.isAfter(dispatch.getTime())) {
                                        canDeliverAll = false;
                                        break;
                                    }
                                }
                            }

                            if (canDeliverAll) {
                                return true; // This time slot works
                            }
                        }
                    }
                    return false; // Drone found but no suitable time slot
                }
            }
        }
        return false; // Drone not found in assignments
    }

    /**
     * Counts the number of moves (transitions) in a drone's path.
     * Each move is a transition between two consecutive positions.
     * @param path
     * @return
     */
    private int countMoves(DronePath path) {
        int moves = 0;
        for (Delivery delivery : path.getDeliveries()) {
            int positions = delivery.getFlightPath().size();
            // Moves = transitions between positions
            if (positions > 0) {
                moves += positions - 1;
            }
        }
        return moves;
    }

    /**
     *calculates flight path for a single drone visiting multiple deliveries
     * @param drone
     * @param sp
     * @param dispatches
     * @param restrictedAreas
     * @return
     */
    private DronePath calculatePath(Drone drone, ServicePoint sp, List<MedDispatchRec> dispatches, List<RestrictedArea> restrictedAreas){
        List<Delivery> deliveries = new ArrayList<>();
        Position current = sp.getLocation();

        for (int i = 0; i < dispatches.size(); i++) {
            MedDispatchRec dispatch = dispatches.get(i);
            List<Position> flightPath = calculateFlightPath(current, dispatch.getDelivery(), restrictedAreas);

            // Add TWO duplicate positions at the end to mark delivery being made
            flightPath.add(dispatch.getDelivery());
            flightPath.add(dispatch.getDelivery());

            deliveries.add(new Delivery(dispatch.getId(), flightPath));

            // Next delivery starts at this delivery point
            current = dispatch.getDelivery();
        }

        // Calculate return path to service point as a separate delivery with null ID
        List<Position> returnPath = calculateFlightPath(current, sp.getLocation(), restrictedAreas);
        if (!returnPath.isEmpty()) {
            // Create separate Delivery object with deliveryId = null for return journey
            deliveries.add(new Delivery(null, returnPath));
        }

        return new DronePath(drone.getId(), deliveries);
    }

    /**
     * A* search that finds optimal path from start to end
     * avoids restricted areas
     * @param start
     * @param end
     * @param restrictedAreas
     * @return
     */
    private List<Position> calculateFlightPath(Position start, Position end, List<RestrictedArea> restrictedAreas){
        // Quick check: if no restricted areas, use simple greedy path
        if (restrictedAreas.isEmpty()) {
            return fallbackGreedyPath(start, end, restrictedAreas);
        }

        // Try greedy path first - it's much faster
        List<Position> greedyPath = fallbackGreedyPath(start, end, restrictedAreas);
        if (!greedyPath.isEmpty() && greedyPath.size() < 10000) {
            // Greedy found a reasonable path, use it
            return greedyPath;
        }

        // Fall back to A* only if greedy fails
        //queue to track nodes to be visited
        PriorityQueue<Node> openSet = new PriorityQueue<>((a,b) -> Double.compare(a.f, b.f));
        //visited nodes set
        HashSet<String> closedSet = new HashSet<>();
        //all nodes, for updating when better path found
        Map<String, Node> allNodes = new HashMap<>();

        //starting Node
        Node startNode = new Node(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(posKey(start), startNode);

        // Reduced iteration limit for faster performance
        int maxIterations = 50000;
        int iterations = 0;

        while(!openSet.isEmpty() && iterations < maxIterations){
            iterations++;

            //checks node with the lowest f(n)
            Node current = openSet.poll();
            //check if we have reached the destination
            if(distanceService.isCloseTo(current.pos, end)){
                return reconstructPath(current);
            }
            //mark the node as visited
            String currentKey = posKey(current.pos);
            closedSet.add(currentKey);

            // explore all 16 compass directions from the current position
            for (double angle : VALID_ANGLES){
                Position neighbor = distanceService.nextPosition(current.pos, angle);
                String neighborKey = posKey(neighbor);

                // skip if the node is already visited, prevents loops
                if (closedSet.contains(neighborKey)){
                    continue;
                }

                // check if the move would violate restricted area, either is inside it or path would cross through it
                if (isBlocked(current.pos, neighbor, restrictedAreas)){
                    continue;
                }

                // calculate cost to move to neighbour
                double moveCost = distanceService.euclideanDistance(current.pos, neighbor);
                double tentativeG = current.g + moveCost;

                Node neighborNode = allNodes.get(neighborKey);

                // either unvisited or there is better path
                if (neighborNode == null){
                    //unvisited node
                    double h = heuristic(neighbor, end);
                    neighborNode = new Node(neighbor, current, tentativeG, h);
                    //add to sets
                    allNodes.put(neighborKey, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.g){
                    //cheaper path found
                    neighborNode.parent = current;
                    neighborNode.g = tentativeG;
                    neighborNode.f = neighborNode.g + neighborNode.h;

                    // update the priority queue with new cost
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        // if A* dosent find path, fallback search
        return fallbackGreedyPath(start, end, restrictedAreas);
    }

    /**
     * Heuristic used in A*, euclidean distance to the goal
     * is admissable as straight line distance never overestimates, is the optimal path
     * @param current
     * @param goal
     * @return
     */
    private double heuristic(Position current, Position goal){
        return distanceService.euclideanDistance(current, goal);
    }

    /**
     * reconstructs the path by following node parent pointer from the goal to the start
     * @param node
     * @return
     */
    private List<Position> reconstructPath(Node node){
        List<Position> path = new ArrayList<>();
        Node current = node;

        while (current != null){
            path.add(0, current.pos);
            current = current.parent;
        }

        return path;
    }


    private List<Position> fallbackGreedyPath(Position start, Position end, List<RestrictedArea> restrictedAreas){
        List<Position> path = new ArrayList<>();
        Position current = start;
        path.add(current);

        int maxIterations = 10000; // Reduced for faster performance

        while (!distanceService.isCloseTo(current, end) && path.size() < maxIterations){
            double bestAngle = findBestAngle(current, end, restrictedAreas);
            current = distanceService.nextPosition(current, bestAngle);
            path.add(current);
        }

        return path;
    }

    /**
     * converts a position into a string key allowing position to be used for hashmap key
     * @param p
     * @return
     */
    private String posKey(Position p){
        return Math.round(p.getLat() * 1e4) + "," + Math.round(p.getLng() * 1e4);
    }

    /**
     * picks compass direction that minimises distance to the goal
     * used by the fallback search
     * @param current
     * @param target
     * @param restrictedAreas
     * @return
     */
    private double findBestAngle(Position current, Position target, List<RestrictedArea> restrictedAreas){
        double bestAngle = 0;
        double minDistance = Double.MAX_VALUE;

        for (double angle : VALID_ANGLES){
            Position nextPos = distanceService.nextPosition(current, angle);

            if (isBlocked(current, nextPos, restrictedAreas)){
                continue;
            }

            double distToTarget = distanceService.euclideanDistance(nextPos, target);

            if (distToTarget < minDistance){
                minDistance = distToTarget;
                bestAngle = angle;
            }
        }

        return bestAngle;
    }

    /**
     * checks if the move would violate any restricted areas
     * is destination inside a restricted area?
     * does the path from -> to cross a restricted area
     * @param from
     * @param to
     * @param restrictedAreas
     * @return
     */
    private boolean isBlocked(Position from, Position to, List<RestrictedArea> restrictedAreas){
        for (RestrictedArea area : restrictedAreas){
            Region region = new Region(area.getName(), area.getVertices());
            if (distanceService.isInRegion(to, region)){
                return true;
            }
            if (lineSegmentCrossesPolygon(from, to, area.getVertices())){
                return true;
            }
        }
        return false;
    }

    /**
     * check if a line crosses any edge of the polygon
     * @param p1
     * @param p2
     * @param polygon
     * @return
     */
    private boolean lineSegmentCrossesPolygon(Position p1, Position p2, List<Position> polygon){
        for (int i = 0; i < polygon.size() - 1; i++){
            Position v1 = polygon.get(i);
            Position v2 = polygon.get(i + 1);

            if (segmentsIntersect(p1, p2, v1, v2)){
                return true;
            }
        }
        return false;
    }

    private boolean segmentsIntersect(Position a, Position b, Position c, Position d){
        return ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d);
    }

    /**
     * counter clockwise orientation test
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private boolean ccw(Position a, Position b, Position c){
        return (c.getLng() - a.getLng()) * (b.getLat() - a.getLat()) >
                (b.getLng() - a.getLng()) * (c.getLat() - a.getLat());
    }

    /**
     * checks if a drone can deliver a singular dispatch
     * @param drone
     * @param dispatch
     * @return
     */
    private boolean canDeliverSingleDispatch(Drone drone, MedDispatchRec dispatch){
        Drone.Capability cap = drone.getCapability();
        MedDispatchRec.Requirements req = dispatch.getRequirements();

        if (cap.getCapacity() < req.getCapacity()){
            return false;
        }

        if (req.isCooling() && !cap.isCooling()){
            return false;
        }

        if (req.isHeating() && !cap.isHeating()){
            return false;
        }

        return true;
    }

    /**
     * Find the service point assigned to a specific drone
     * @param droneId The drone ID to look up
     * @param assignments The drone-to-service-point assignments
     * @param servicePoints All available service points
     * @return The service point for this drone, or null if not found
     */
    private ServicePoint getServicePointForDrone(String droneId,
                                                  List<DroneForServicePointResponse> assignments,
                                                  List<ServicePoint> servicePoints) {
        for (DroneForServicePointResponse assignment : assignments) {
            for (DroneAvailability availability : assignment.getDrones()) {
                if (availability.getId().equals(droneId)) {
                    Integer servicePointId = assignment.getServicePointId();
                    return servicePoints.stream()
                            .filter(sp -> sp.getId().equals(servicePointId))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
        return null;
    }



    /**
     * Build an optimal multi-delivery route for a drone
     * Uses nearest-neighbor heuristic to order deliveries efficiently
     * Validates capacity and distance constraints
     *
     * @param drone The drone to build route for
     * @param servicePoint The service point where drone starts/ends
     * @param candidates List of potential dispatches
     * @param restrictedAreas No-fly zones to avoid
     * @return Ordered list of dispatches for this route
     */
    private List<MedDispatchRec> buildOptimalRoute(Drone drone,
                                                     ServicePoint servicePoint,
                                                     List<MedDispatchRec> candidates,
                                                     List<RestrictedArea> restrictedAreas) {
        List<MedDispatchRec> route = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        Position current = servicePoint.getLocation();
        double accumulatedCapacity = 0;
        int estimatedMoves = 0;

        System.out.println("DEBUG buildOptimalRoute: Drone " + drone.getId() +
            " capacity=" + drone.getCapability().getCapacity() +
            ", maxMoves=" + drone.getCapability().getMaxMoves() +
            ", candidates=" + candidates.size());

        // Performance optimization: limit multi-delivery attempts
        // Try single delivery first (fastest), then attempt multi-delivery
        int maxDeliveriesPerDrone = Math.min(3, candidates.size()); // Limit to 3 deliveries max

        // Greedy nearest-neighbor selection
        while (route.size() < maxDeliveriesPerDrone && route.size() < candidates.size()) {
            MedDispatchRec nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (MedDispatchRec candidate : candidates) {
                if (used.contains(candidate.getId())) {
                    continue;
                }

                // Check capacity constraint FIRST (cheap check)
                double newCapacity = accumulatedCapacity + candidate.getRequirements().getCapacity();
                if (newCapacity > drone.getCapability().getCapacity()) {
                    continue; // Would exceed drone capacity
                }

                // Estimate distance (Euclidean as lower bound)
                double distance = distanceService.euclideanDistance(current, candidate.getDelivery());

                // Early check: would this exceed move budget?
                double estMovesToTarget = distance / 0.00015;
                double estReturnMoves = distanceService.euclideanDistance(candidate.getDelivery(), servicePoint.getLocation()) / 0.00015;

                if (estimatedMoves + estMovesToTarget + estReturnMoves > drone.getCapability().getMaxMoves() * 0.9) {
                    // Would likely exceed moves, skip
                    continue;
                }

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                }
            }

            if (nearest == null) {
                break; // Can't add more deliveries
            }

            // Estimate moves BEFORE adding (early rejection)
            double distMoves = minDistance / 0.00015;
            double returnDistance = distanceService.euclideanDistance(nearest.getDelivery(), servicePoint.getLocation());
            double returnMoves = returnDistance / 0.00015;

            if (estimatedMoves + distMoves + returnMoves > drone.getCapability().getMaxMoves()) {
                // Would exceed budget, stop here
                break;
            }

            // Add to route
            route.add(nearest);
            used.add(nearest.getId());
            accumulatedCapacity += nearest.getRequirements().getCapacity();
            estimatedMoves += (int) distMoves;
            current = nearest.getDelivery();
        }

        System.out.println("DEBUG buildOptimalRoute: Final route size=" + route.size() +
            ", estimated moves=" + estimatedMoves);

        return route;
    }

    /**
     * converts calculated delivery path to GeoJSON
     * @param dispatches
     * @return
     */
    public String calcDeliveryPathAsGeoJson(List<MedDispatchRec> dispatches) {
        CalcDeliveryPathResponse response = calcDeliveryPath(dispatches);

        List<List<Double>> coordinates = new ArrayList<>();

        for (DronePath dronePath : response.getDronePaths()) {
            for (Delivery delivery : dronePath.getDeliveries()) {
                for (Position pos : delivery.getFlightPath()) {
                    List<Double> coord = new ArrayList<>();
                    coord.add(pos.getLng());  // GeoJSON: [lng, lat]
                    coord.add(pos.getLat());
                    coordinates.add(coord);
                }
            }
        }

        Map<String, Object> geoJson = new LinkedHashMap<>();
        geoJson.put("type", "LineString");
        geoJson.put("coordinates", coordinates);

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(geoJson);
        } catch (Exception e) {
            return "{\"type\": \"LineString\", \"coordinates\": []}";
        }
    }


    /**
     * Node class used for A* search
     * represents a position in the graph
     */
    private static class Node {
        Position pos; //the position of this node object with lat, lng
        Node parent; //the previous node in the path
        double g;  //distance from start to this node
        double h;  //estimated distance from the node to the goal - uses euclidean distance
        double f;  // g+h

        Node(Position pos, Node parent, double g, double h){
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }

}


