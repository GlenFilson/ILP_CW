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
    @NotNull
    private String name;
    @NotNull
    @Min(0)//id cannot be negative
    private Integer id;
    @NotNull
    private Capability capability;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Capability {
        private boolean cooling;
        private boolean heating;
        private Integer capacity;
        private Integer maxMoves;
        private Double costPerMove;
        private Double costInitial;
        private Double costFinal;
    }
}

