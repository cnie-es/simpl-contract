package eu.europa.ec.simpl.contracts.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequestTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String status;

}
