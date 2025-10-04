package uk.ac.ed.acp.cw2.dto;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.model.Position;
import uk.ac.ed.acp.cw2.model.Region;

public class IsInRegionRequest {
    @Getter
    @Setter
    Position position;

    @Getter @Setter
    Region region;

}
