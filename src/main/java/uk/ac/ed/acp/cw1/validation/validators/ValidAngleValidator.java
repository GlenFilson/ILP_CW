package uk.ac.ed.acp.cw1.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.ac.ed.acp.cw1.validation.annotations.ValidAngle;

public class ValidAngleValidator implements ConstraintValidator<ValidAngle, Double> {

    private static final double VALID_ANGLE_MULTIPLE = 22.5;

    @Override
    public boolean isValid(Double angle, ConstraintValidatorContext context) {
        if (angle == null) return false;
        return angle % VALID_ANGLE_MULTIPLE == 0;
    }
}
