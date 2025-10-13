package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IsInRegionRequest {
    @Valid
    @NotNull(message = "position cannot be null")
    private Position position;

    @Valid
    @NotNull(message = "region cannot be null")
    private Region region;
}
