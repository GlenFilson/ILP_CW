package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestrictedArea {
    private String name;
    private Integer id;
    private List<Position> vertices;
}
