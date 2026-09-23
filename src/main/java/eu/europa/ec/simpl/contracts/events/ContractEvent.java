package eu.europa.ec.simpl.contracts.events;

import eu.europa.ec.simpl.contracts.transfer.Mode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private UUID contractAgreementId;

    @Valid
    @NotNull
    private Mode mode;
}
