package uk.ac.ed.acp.cw1.unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.validation.validators.ClosedPolygonValidator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClosedPolygonValidatorTests {

    private ClosedPolygonValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClosedPolygonValidator();
    }



    @Test
    public void testClosedPolygon_shouldReturnTrue() {
        List<Position> closedPolygon = List.of(
                new Position(0.0, 0.0),
                new Position(2.0, 1.0),
                new Position(3.0, 3.0),
                new Position(1.5, 4.0),
                new Position(0.0, 3.0),
                new Position(-1.0, 1.5),
                new Position(0.0, 0.0)
        );
        assertTrue(validator.isValid(closedPolygon, null));

    }

    @Test
    public void testClosedPolygon_shouldReturnFalse() {
        List<Position> openPolygon = List.of(
                new Position(0.0, 0.0),
                new Position(2.0, 1.0),
                new Position(3.0, 3.0),
                new Position(1.5, 4.0),
                new Position(0.0, 3.0),
                new Position(-1.0, 1.5),
                new Position(0.01, 0.0)
        );
        assertFalse(validator.isValid(openPolygon, null));

    }

}
