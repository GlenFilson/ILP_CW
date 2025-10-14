package uk.ac.ed.acp.cw1.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.ac.ed.acp.cw1.dto.Position;
import uk.ac.ed.acp.cw1.validation.annotations.ClosedPolygon;

import java.util.List;

public class ClosedPolygonValidator implements ConstraintValidator<ClosedPolygon, List<Position>> {


    @Override
    public boolean isValid(List<Position> vertices, ConstraintValidatorContext context) {
        //validator should return true if null, so the other beans are responsible for checks
        if (vertices == null || vertices.isEmpty()) return true;
        return vertices.getFirst().getLng().equals(vertices.getLast().getLng())
                && vertices.getFirst().getLat().equals(vertices.getLast().getLat());
    }
}
