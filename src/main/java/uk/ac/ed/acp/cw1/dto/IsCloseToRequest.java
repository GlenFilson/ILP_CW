package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IsCloseToRequest {
    @Valid
    @NotNull(message = "position1 cannot be null")
    private Position position1;

    @Valid
    @NotNull(message = "position2 cannot be null")
    private Position position2;
}
