package uk.ac.ed.acp.cw1.service;

import uk.ac.ed.acp.cw1.dto.Drone;
import uk.ac.ed.acp.cw1.dto.QueryAttribute;

public class AttributeComparator {

    /**
     * Checks if a {@link Drone} matches the given {@link QueryAttribute}
     * is used by the DroneService for the queryAsPath function
     * @param drone
     * @param attribute
     * @return boolean true if match false if not
     */
    public static boolean matches(Drone drone, QueryAttribute attribute){
        Drone.Capability capability = drone.getCapability();
        String attributeName = attribute.getAttribute();
        String operator = attribute.getOperator();
        String attributeValue = attribute.getValue();

        return switch (attributeName) {
            case "capacity" -> compareNumeric(capability.getCapacity(), attributeValue, operator);
            case "maxmoves" -> compareNumeric(capability.getMaxMoves(), attributeValue, operator);
            case "costpermove" -> compareNumeric(capability.getCostPerMove(), attributeValue, operator);
            case "costinitial" -> compareNumeric(capability.getCostInitial(), attributeValue, operator);
            case "costfinal" -> compareNumeric(capability.getCostFinal(), attributeValue, operator);
            case "cooling" -> compareBoolean(capability.isCooling(), attributeValue, operator);
            case "heating" -> compareBoolean(capability.isHeating(), attributeValue, operator);
            default -> false;
        };
    }

    /**
     * Use number to handle all numeric types, int and double etc..
     * @param droneValue
     * @param expectedValue
     * @param operator accepts =, !, <, >
     * @return
     */
    private static boolean compareNumeric(Number droneValue, String expectedValue, String operator ){
        try {
            double dValue = droneValue.doubleValue();
            double eValue = Double.parseDouble(expectedValue);

            return switch (operator) {
                case "=" -> dValue == eValue;
                case "!=" -> dValue != eValue;
                case "<" -> dValue < eValue;
                case ">" -> dValue > eValue;
                default -> false;
            };
        }catch (NumberFormatException e){
            return false;
        }
    }

    /**
     *
     * @param droneValue
     * @param expectedValue
     * @param operator accepts =, !=
     * @return
     */
    private static boolean compareBoolean(boolean droneValue, String expectedValue, String operator){
        boolean eValue = Boolean.parseBoolean(expectedValue);
        return switch(operator){
            case "=" -> droneValue == eValue;
            case "!=" -> droneValue != eValue;
            default -> false;
        };
    }
}
