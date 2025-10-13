package uk.ac.ed.acp.cw1.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.ac.ed.acp.cw1.validation.validators.ValidAngleValidator;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAngleValidator.class)
public @interface ValidAngle {
    String message() default "Angle must be a multiple of valid step";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
