package uk.ac.ed.acp.cw1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAttribute {
    @NotNull
    private String attribute;
    //dont think operator can ever be null as even in queryAsPath it is set to "="
    @NotNull
    private String operator;
    @NotNull
    private String value;

}
