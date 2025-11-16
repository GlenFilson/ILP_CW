package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Drone {
    @NotNull(message="Drone name cannot be null")
    private String name;
    @NotNull(message="Drone id cannot be null")
    //changed from int to string as said in announcement
    private String id;
    @NotNull(message="Drone capability cannot be null")
    private Capability capability;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Capability {
        private boolean cooling;
        private boolean heating;
        private Double capacity;
        private Integer maxMoves;
        private Double costPerMove;
        private Double costInitial;
        private Double costFinal;
    }
}

