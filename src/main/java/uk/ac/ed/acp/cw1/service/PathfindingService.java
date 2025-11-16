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


        //allocate dispatches, assign dispatches to drones before pathfinding. e.g. drone A does 1, 4, 5, drone B does 2, 3, 6
        Map<String, List<MedDispatchRec>> allocations = new HashMap<>();

        for (MedDispatchRec dispatch : dispatches){
            for (Drone drone : drones){
                if (canDeliverSingleDispatch(drone, dispatch)){
                    allocations.computeIfAbsent(drone.getId(), k -> new ArrayList<>()).add(dispatch);
                    break;//break on first drone that works - fast
                }
            }
        }

        List<DronePath> dronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMoves = 0;

        ServicePoint sp = servicePoints.getFirst(); //gets the first service point - fast
        //process each drones assigned deliveries
        for (Map.Entry<String, List<MedDispatchRec>> entry : allocations.entrySet()){
            String droneId = entry.getKey();
            List<MedDispatchRec> assigned = entry.getValue();

            Drone drone = droneService.getDroneById(droneId);
            if (drone != null){
                DronePath path = calculatePath(drone, sp, assigned, restrictedAreas);
                dronePaths.add(path);


                int pathMoves = countMoves(path);
                //cost = initial + (moves*cost-per-move) + final
                double pathCost = drone.getCapability().getCostInitial()
                        + (pathMoves * drone.getCapability().getCostPerMove())
                        + drone.getCapability().getCostFinal();

                totalCost += pathCost;
                totalMoves += pathMoves;
            }
        }


    return new CalcDeliveryPathResponse(totalCost, totalMoves, dronePaths);
    }

    /**
     * counts the number of points in a drones path
     * @param path
     * @return
     */
    private int countMoves(DronePath path) {
        int moves = 0;
        for (Delivery delivery : path.getDeliveries()) {
            moves += delivery.getFlightPath().size();
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

        for (MedDispatchRec dispatch : dispatches){
            List<Position> flightPath = calculateFlightPath(current, dispatch.getDelivery(), restrictedAreas);
            //add the hover point, duplicate position that marks a delivery being made
            flightPath.add(dispatch.getDelivery());
            deliveries.add(new Delivery(dispatch.getId(), flightPath));
            current = dispatch.getDelivery();
        }

        List<Position> returnPath = calculateFlightPath(current, sp.getLocation(), restrictedAreas);

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

        while(!openSet.isEmpty()){
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

        int maxIterations = 50000;

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
        return p.getLat() + "," + p.getLng();
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


