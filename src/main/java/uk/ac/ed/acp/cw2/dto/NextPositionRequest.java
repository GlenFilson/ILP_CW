package uk.ac.ed.acp.cw2.dto;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.model.Position;

public class NextPositionRequest {
    @Getter
    @Setter
    Position start;

    @Getter @Setter
    Double angle;
}
