package uk.ac.ed.acp.cw2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Region {
    @Getter
    @Setter
    String name;
    @Getter @Setter
    List<Position> vertices;

}
