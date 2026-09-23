package eu.europa.ec.simpl.contracts.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractSearchTO implements Serializable {

    private static final String STRING_VALIDATION_REGEXP = "^(?U)[\\w:-]+$";

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID contractAgreementId;

    @Valid
    @Pattern(
            regexp = STRING_VALIDATION_REGEXP,
            message = "contractDefinitionId may contain only alphanumeric characters and colons"
    )
    private String contractDefinitionId;

    @Valid
    @Pattern(
            regexp = STRING_VALIDATION_REGEXP,
            message = "assetId should be string"
    )
    private String assetId;

    @Valid

    @Pattern(
            regexp = STRING_VALIDATION_REGEXP,
            message = "providerId should be string"
    )
    private String providerId;

    @Valid
    @Pattern(
            regexp = STRING_VALIDATION_REGEXP,
            message = "consumerId should be string"
    )
    private String consumerId;
}
