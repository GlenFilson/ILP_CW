package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import uk.ac.ed.acp.cw1.validation.annotations.ValidAngle;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NextPositionRequest {
    @Valid
    @NotNull(message = "start position cannot be null")
    private Position start;

    @Valid
    @NotNull(message = "angle cannot be null")
    @ValidAngle
    @Range(min = 0, max = 360)
    private Double angle;
}
