package uk.ac.ed.acp.cw2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.model.Position;
public class NextPositionResponse {
    @Getter @Setter
    Double lat;
    @Getter @Setter
    Double lng;

}
