package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneAvailability{
    //changed from int to String as per announcement
    private String id;//the drone ID
    private List<Availability> availability;

}
