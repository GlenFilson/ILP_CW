package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    //the dispatchID from the MedDispatchRec, the delivery request that is being fulfilled
    private Integer deliveryId;
    //the path the drone takes to get to fulfill this delivery, the list of positions made
    private List<Position> flightPath;
}
