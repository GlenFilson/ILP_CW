
package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    @NotNull(message = "Latitude cannot be null")
    private Double lat;
    @NotNull(message = "Longitude cannot be null")
    private Double lng;

}
