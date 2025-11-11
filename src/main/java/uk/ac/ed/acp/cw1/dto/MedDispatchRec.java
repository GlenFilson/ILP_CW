package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedDispatchRec {
    @NotNull
    private Integer id;
    private LocalDate date;
    private LocalTime time;
    @NotNull //Assuming that if capacity is required then requirements must be
    private Requirements requirements;
    @NotNull //Check if this should stay as a required field?
    private Position delivery;//delivery location



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Requirements{
        @NotNull
        private Double capacity;
        private boolean cooling;
        private boolean heating;
        private Double maxCost;
    }
}


