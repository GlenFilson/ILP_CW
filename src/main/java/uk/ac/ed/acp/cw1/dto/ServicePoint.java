package uk.ac.ed.acp.cw1.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ServicePoint {
    @NotNull
    private String name;
    @NotNull
    private Integer id;
    @NotNull
    //currently I am assuming that this can still be of type Position even though current Position class does not have alt
    //assumes alt is not needed/relevant
    private Position location;

}
