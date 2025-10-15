
package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    @NotNull(message = "Latitude cannot be null")
    @Range(min = -90, max = 90)
    private Double lat;
    @NotNull(message = "Longitude cannot be null")
    @Range(min = -180, max = 180)
    private Double lng;

}
