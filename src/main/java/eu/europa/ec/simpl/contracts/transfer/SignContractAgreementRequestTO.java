package eu.europa.ec.simpl.contracts.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class SignContractAgreementRequestTO {

    @JsonProperty("context")
    private List<String> contexts;
    @JsonProperty("credentialSubject")
    private Map<String, String> credentialSubjectData;
    private String format;
    private String group;
    private String key;
    private String namespace;
    private String signatureType;
    private boolean status;
}
