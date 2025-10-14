package uk.ac.ed.acp.cw1.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.ac.ed.acp.cw1.validation.annotations.ValidAngle;

public class ValidAngleValidator implements ConstraintValidator<ValidAngle, Double> {

    private static final double VALID_ANGLE_MULTIPLE = 22.5;

    @Override
    public boolean isValid(Double angle, ConstraintValidatorContext context) {
        //validator should return true if null, so the other beans are responsible for checks
        if (angle == null) return true;
        return angle % VALID_ANGLE_MULTIPLE == 0;
    }
}
