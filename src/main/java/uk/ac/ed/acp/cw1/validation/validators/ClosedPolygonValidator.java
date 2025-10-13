package uk.ac.ed.acp.cw1.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.validation.annotations.ClosedPolygon;

import java.util.List;

public class ClosedPolygonValidator implements ConstraintValidator<ClosedPolygon, List<Position>> {


    @Override
    public boolean isValid(List<Position> vertices, ConstraintValidatorContext context) {
        return vertices.getFirst().getLng().equals(vertices.getLast().getLng())
                && vertices.getFirst().getLat().equals(vertices.getLast().getLat());
    }
}
