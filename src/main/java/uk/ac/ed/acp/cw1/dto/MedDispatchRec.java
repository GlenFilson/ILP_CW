package uk.ac.ed.acp.cw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedDispatchRec {

    private Integer id;
    private LocalDate date;
    private LocalTime time;
    private List<Requirements> requirements;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Requirements{
        private Double capacity;
        private boolean cooling;
        private boolean heating;
        private Double maxCost;
    }
}


