package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneForServicePointResponse {
    private Integer servicePointId;
    private List<DroneAvailability> drones;

}
