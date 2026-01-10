package uk.ac.ed.acp.cw1.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw1.dto.Drone;
import uk.ac.ed.acp.cw1.dto.QueryAttribute;
import uk.ac.ed.acp.cw1.service.AttributeComparator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttributeComparator Unit Tests")
public class AttributeComparatorTests {

    private Drone testDrone;

    @BeforeEach
    void setUp() {
        testDrone = createTestDrone("D001", 15.0, 1000, true, false, 0.5, 2.0, 1.5);
    }

    // ===============================================
    // HELPER METHODS
    // ===============================================

    private Drone createTestDrone(String id, double capacity, int maxMoves,
                                   boolean cooling, boolean heating,
                                   double costPerMove, double costInitial, double costFinal) {
        Drone drone = new Drone();
        drone.setId(id);
        drone.setName("TestDrone-" + id);
        Drone.Capability cap = new Drone.Capability();
        cap.setCapacity(capacity);
        cap.setMaxMoves(maxMoves);
        cap.setCooling(cooling);
        cap.setHeating(heating);
        cap.setCostPerMove(costPerMove);
        cap.setCostInitial(costInitial);
        cap.setCostFinal(costFinal);
        drone.setCapability(cap);
        return drone;
    }

    private QueryAttribute createQueryAttribute(String attribute, String operator, String value) {
        return new QueryAttribute(attribute, operator, value);
    }

    // ===============================================
    // 1. EQUALITY OPERATOR (=) TESTS
    // ===============================================

    @Test
    @DisplayName("Equality operator: Matches exact capacity value")
    void testEquality_capacityExactMatch_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", "=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Equality operator: Does not match different capacity value")
    void testEquality_capacityNoMatch_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "=", "20.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Equality operator: Matches exact maxMoves value")
    void testEquality_maxMovesExactMatch_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("maxMoves", "=", "1000");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Equality operator: Matches cooling=true")
    void testEquality_coolingTrue_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("cooling", "=", "true");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Equality operator: Does not match cooling=false when drone has cooling")
    void testEquality_coolingFalse_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("cooling", "=", "false");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Equality operator: Matches heating=false")
    void testEquality_heatingFalse_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("heating", "=", "false");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 2. NOT EQUALS OPERATOR (!=) TESTS
    // ===============================================

    @Test
    @DisplayName("Not equals operator: Returns true for different capacity")
    void testNotEquals_capacityDifferent_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", "!=", "20.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Not equals operator: Returns false for same capacity")
    void testNotEquals_capacitySame_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "!=", "15.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Not equals operator: Returns true for heating!=true when heating is false")
    void testNotEquals_heatingNotTrue_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("heating", "!=", "true");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Not equals operator: Returns false for cooling!=true when cooling is true")
    void testNotEquals_coolingNotTrue_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("cooling", "!=", "true");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 3. GREATER THAN OPERATOR (>) TESTS
    // ===============================================

    @Test
    @DisplayName("Greater than operator: Returns true when capacity > value")
    void testGreaterThan_capacityGreater_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "10.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Greater than operator: Returns false when capacity == value")
    void testGreaterThan_capacityEqual_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "15.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Greater than operator: Returns false when capacity < value")
    void testGreaterThan_capacityLess_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "20.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Greater than operator: Works with maxMoves")
    void testGreaterThan_maxMoves_returnsCorrectly() {
        QueryAttribute attr = createQueryAttribute("maxMoves", ">", "500");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 4. LESS THAN OPERATOR (<) TESTS
    // ===============================================

    @Test
    @DisplayName("Less than operator: Returns true when capacity < value")
    void testLessThan_capacityLess_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", "<", "20.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Less than operator: Returns false when capacity == value")
    void testLessThan_capacityEqual_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "<", "15.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Less than operator: Returns false when capacity > value")
    void testLessThan_capacityGreater_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "<", "10.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Less than operator: Works with costPerMove")
    void testLessThan_costPerMove_returnsCorrectly() {
        QueryAttribute attr = createQueryAttribute("costPerMove", "<", "1.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 5. GREATER THAN OR EQUAL OPERATOR (>=) TESTS
    // ===============================================

    @Test
    @DisplayName("Greater than or equal operator: Returns true when capacity > value")
    void testGreaterThanOrEqual_capacityGreater_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", ">=", "10.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Greater than or equal operator: Returns true when capacity == value")
    void testGreaterThanOrEqual_capacityEqual_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", ">=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Greater than or equal operator: Returns false when capacity < value")
    void testGreaterThanOrEqual_capacityLess_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", ">=", "20.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 6. LESS THAN OR EQUAL OPERATOR (<=) TESTS
    // ===============================================

    @Test
    @DisplayName("Less than or equal operator: Returns true when capacity < value")
    void testLessThanOrEqual_capacityLess_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", "<=", "20.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Less than or equal operator: Returns true when capacity == value")
    void testLessThanOrEqual_capacityEqual_returnsTrue() {
        QueryAttribute attr = createQueryAttribute("capacity", "<=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Less than or equal operator: Returns false when capacity > value")
    void testLessThanOrEqual_capacityGreater_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "<=", "10.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 7. TYPE CONVERSION TESTS
    // ===============================================

    @Test
    @DisplayName("Type conversion: Integer string parsed correctly for maxMoves")
    void testTypeConversion_integerString_parsedCorrectly() {
        QueryAttribute attr = createQueryAttribute("maxMoves", "=", "1000");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Type conversion: Double string parsed correctly for capacity")
    void testTypeConversion_doubleString_parsedCorrectly() {
        QueryAttribute attr = createQueryAttribute("capacity", "=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Type conversion: Boolean true string parsed correctly")
    void testTypeConversion_booleanTrueString_parsedCorrectly() {
        QueryAttribute attr = createQueryAttribute("cooling", "=", "true");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Type conversion: Boolean false string parsed correctly")
    void testTypeConversion_booleanFalseString_parsedCorrectly() {
        QueryAttribute attr = createQueryAttribute("heating", "=", "false");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Type conversion: Invalid numeric format returns false")
    void testTypeConversion_invalidNumericFormat_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "=", "abc");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Type conversion: Invalid numeric format for greater than returns false")
    void testTypeConversion_invalidNumericFormatGreaterThan_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "not-a-number");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 8. ATTRIBUTE NAME TESTS
    // ===============================================

    @Test
    @DisplayName("Attribute name: Case insensitive matching (CAPACITY)")
    void testAttributeName_caseInsensitive_uppercase() {
        QueryAttribute attr = createQueryAttribute("CAPACITY", "=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name: Case insensitive matching (Capacity)")
    void testAttributeName_caseInsensitive_mixedCase() {
        QueryAttribute attr = createQueryAttribute("Capacity", "=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name: Unknown attribute returns false")
    void testAttributeName_unknownAttribute_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("unknownAttribute", "=", "value");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name: costPerMove comparison works")
    void testAttributeName_costPerMove_works() {
        QueryAttribute attr = createQueryAttribute("costPerMove", "=", "0.5");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name: costInitial comparison works")
    void testAttributeName_costInitial_works() {
        QueryAttribute attr = createQueryAttribute("costInitial", "=", "2.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name: costFinal comparison works")
    void testAttributeName_costFinal_works() {
        QueryAttribute attr = createQueryAttribute("costFinal", "=", "1.5");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 9. INVALID OPERATOR TESTS
    // ===============================================

    @Test
    @DisplayName("Invalid operator: Unknown operator returns false for numeric")
    void testInvalidOperator_unknownOperatorNumeric_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "~", "15.0");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Invalid operator: Unknown operator returns false for boolean")
    void testInvalidOperator_unknownOperatorBoolean_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("cooling", ">", "true");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Invalid operator: Less than on boolean returns false")
    void testInvalidOperator_lessThanOnBoolean_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("cooling", "<", "true");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    // ===============================================
    // 10. EDGE CASE TESTS
    // ===============================================

    @Test
    @DisplayName("Edge case: Zero value comparison works")
    void testEdgeCase_zeroValue_works() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Edge case: Negative value comparison works")
    void testEdgeCase_negativeValue_works() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "-10.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Edge case: Very large value comparison works")
    void testEdgeCase_veryLargeValue_works() {
        QueryAttribute attr = createQueryAttribute("capacity", "<", "1000000.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Edge case: Empty string value returns false for numeric")
    void testEdgeCase_emptyStringNumeric_returnsFalse() {
        QueryAttribute attr = createQueryAttribute("capacity", "=", "");
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Edge case: Boolean with any non-true string is parsed as false")
    void testEdgeCase_booleanNonTrueString_parsedAsFalse() {
        // Boolean.parseBoolean returns false for any non-"true" string
        QueryAttribute attr = createQueryAttribute("heating", "=", "yes");
        assertTrue(AttributeComparator.matches(testDrone, attr)); // heating is false, "yes" parsed as false
    }
}
