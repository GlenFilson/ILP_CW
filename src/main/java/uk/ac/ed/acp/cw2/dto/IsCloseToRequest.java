package uk.ac.ed.acp.cw2.dto;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.model.Position;

public class IsCloseToRequest {
    @Getter @Setter
    private Position position1;
    @Getter @Setter
    private Position position2;
}
