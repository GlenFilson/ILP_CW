package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalcDeliveryPathResponse {

    private Double totalCost;
    private Integer totalMoves;
    //the individual paths that have been calculated
    private List<DronePath> dronePaths;
}
