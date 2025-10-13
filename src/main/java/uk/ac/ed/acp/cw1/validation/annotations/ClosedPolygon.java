package uk.ac.ed.acp.cw1.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.ac.ed.acp.cw1.validation.validators.ClosedPolygonValidator;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClosedPolygonValidator.class)
public @interface ClosedPolygon {
    String message() default "First and last vertices must be equal to form a closed polygon";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
