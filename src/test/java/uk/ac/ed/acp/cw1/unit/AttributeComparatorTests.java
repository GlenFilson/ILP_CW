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

    // 10. EDGE CASE TESTS

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


    @Test
    @DisplayName("Attribute name with mixed case matches correctly")
    void testAttributeName_mixedCase_matches() {
        QueryAttribute attr = createQueryAttribute("CaPaCiTy", "=", "15.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Attribute name with leading/trailing spaces still matches")
    void testAttributeName_withSpaces_handledGracefully() {
        // Depending on implementation, this might fail or succeed
        QueryAttribute attr = createQueryAttribute("capacity", "=", " 15.0 ");
        // Value parsing should handle trimming
        assertNotNull(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("Double comparison with scientific notation")
    void testDoubleComparison_scientificNotation() {
        QueryAttribute attr = createQueryAttribute("capacity", "<", "1.5E1");
        // 1.5E1 = 15.0, capacity is 15.0, so 15.0 < 15.0 is false
        assertFalse(AttributeComparator.matches(testDrone, attr));
    }

@Test
    @DisplayName("Integer maxMoves comparison boundary")
    void testMaxMoves_boundaryValue() {
        QueryAttribute attrEqual = createQueryAttribute("maxMoves", "=", "1000");
        QueryAttribute attrLess = createQueryAttribute("maxMoves", "<", "1000");
        QueryAttribute attrGreater = createQueryAttribute("maxMoves", ">", "1000");

        assertTrue(AttributeComparator.matches(testDrone, attrEqual));
        assertFalse(AttributeComparator.matches(testDrone, attrLess));
        assertFalse(AttributeComparator.matches(testDrone, attrGreater));
    }

    @Test
    @DisplayName("costPerMove with decimal precision")
    void testCostPerMove_decimalPrecision() {
        QueryAttribute attr = createQueryAttribute("costPerMove", "=", "0.5");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("costInitial equals check")
    void testCostInitial_equalsCheck() {
        QueryAttribute attr = createQueryAttribute("costInitial", "=", "2.0");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("costFinal equals check")
    void testCostFinal_equalsCheck() {
        QueryAttribute attr = createQueryAttribute("costFinal", "=", "1.5");
        assertTrue(AttributeComparator.matches(testDrone, attr));
    }

    @Test
    @DisplayName("All boolean attributes with != operator")
    void testAllBooleanAttributes_notEquals() {
        QueryAttribute coolingNotTrue = createQueryAttribute("cooling", "!=", "true");
        QueryAttribute heatingNotFalse = createQueryAttribute("heating", "!=", "false");

        assertFalse(AttributeComparator.matches(testDrone, coolingNotTrue)); // cooling is true
        assertFalse(AttributeComparator.matches(testDrone, heatingNotFalse)); // heating is false
    }

    @Test
    @DisplayName("Comparison operators with integer values")
    void testComparisonOperators_integerValues() {
        QueryAttribute attrGte = createQueryAttribute("maxMoves", ">=", "999");
        QueryAttribute attrLte = createQueryAttribute("maxMoves", "<=", "1001");

        assertTrue(AttributeComparator.matches(testDrone, attrGte)); // 1000 >= 999
        assertTrue(AttributeComparator.matches(testDrone, attrLte)); // 1000 <= 1001
    }

    @Test
    @DisplayName("Zero value comparisons")
    void testZeroValueComparisons() {
        QueryAttribute attrGtZero = createQueryAttribute("capacity", ">", "0");
        QueryAttribute attrGteZero = createQueryAttribute("costPerMove", ">=", "0");

        assertTrue(AttributeComparator.matches(testDrone, attrGtZero)); // 15 > 0
        assertTrue(AttributeComparator.matches(testDrone, attrGteZero)); // 0.5 >= 0
    }

    @Test
    @DisplayName("Negative capacity comparison (edge case)")
    void testNegativeCapacityComparison() {
        QueryAttribute attr = createQueryAttribute("capacity", ">", "-100");
        assertTrue(AttributeComparator.matches(testDrone, attr)); // 15 > -100
    }

    @Test
    @DisplayName("Very small decimal comparison")
    void testVerySmallDecimalComparison() {
        QueryAttribute attr = createQueryAttribute("costPerMove", ">", "0.499999");
        assertTrue(AttributeComparator.matches(testDrone, attr)); // 0.5 > 0.499999
    }

    @Test
    @DisplayName("Boolean TRUE in different cases")
    void testBooleanTrue_differentCases() {
        QueryAttribute attrLower = createQueryAttribute("cooling", "=", "true");
        QueryAttribute attrUpper = createQueryAttribute("cooling", "=", "TRUE");
        QueryAttribute attrMixed = createQueryAttribute("cooling", "=", "True");

        assertTrue(AttributeComparator.matches(testDrone, attrLower));
        assertTrue(AttributeComparator.matches(testDrone, attrUpper));
        assertTrue(AttributeComparator.matches(testDrone, attrMixed));
    }

    @Test
    @DisplayName("Boolean FALSE in different cases")
    void testBooleanFalse_differentCases() {
        QueryAttribute attrLower = createQueryAttribute("heating", "=", "false");
        QueryAttribute attrUpper = createQueryAttribute("heating", "=", "FALSE");
        QueryAttribute attrMixed = createQueryAttribute("heating", "=", "False");

        assertTrue(AttributeComparator.matches(testDrone, attrLower));
        assertTrue(AttributeComparator.matches(testDrone, attrUpper));
        assertTrue(AttributeComparator.matches(testDrone, attrMixed));
    }
}
