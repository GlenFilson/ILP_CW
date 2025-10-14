package uk.ac.ed.acp.cw1.unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw1.validation.validators.ValidAngleValidator;

import static org.junit.jupiter.api.Assertions.*;

public class ValidAngleValidatorTests {

    private ValidAngleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidAngleValidator();
    }

    @Test
    public void testValidAngles_shouldReturnTrue() {
        assertTrue(validator.isValid(0.0, null));
        assertTrue(validator.isValid(22.5, null));
        assertTrue(validator.isValid(45.0, null));
        assertTrue(validator.isValid(360.0, null));
        assertTrue(validator.isValid(337.5, null));
        assertTrue(validator.isValid(382.5, null));
        assertFalse(validator.isValid(383.0, null));
    }

    @Test
    public void testInvalidAngles_shouldReturnFalse() {
        assertFalse(validator.isValid(1.0, null));
        assertFalse(validator.isValid(23.0, null));
        assertFalse(validator.isValid(-22.3, null));
        assertFalse(validator.isValid(361.0, null));
    }

}
