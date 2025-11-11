package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
//the path for a singular drone
public class DronePath {

    private Integer droneId;
    //list of deliveries that are made on this path
    private List<Delivery> deliveries;
    private Double totalCost;
    private Integer totalMoves;

}
