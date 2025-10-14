package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import uk.ac.ed.acp.cw1.validation.annotations.ClosedPolygon;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region {

    @NotNull(message = "name cannot be null")
    private String name;
    @Valid
    @ClosedPolygon
    @Size(min = 4)
    @NotNull(message = "vertices cannot be null")
    private List<Position> vertices;

}
