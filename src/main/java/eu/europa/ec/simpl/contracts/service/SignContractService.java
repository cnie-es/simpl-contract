package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.common.exceptions.ResponseStatusSingleException;
import eu.europa.ec.simpl.common.utils.LogUtils;
import eu.europa.ec.simpl.contracts.transfer.SignContractAgreementRequestTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SignContractService {

    private static final String SIGN_CONTRACT_ENDPOINT = "/v1/credential";

    private final WebClient.RequestBodySpec signerPostSpec;

    @Value("${spring.external-services.urls.signer}")
    private String signerServiceHost;

    public SignContractService(@Qualifier("signerPostSpec") WebClient.RequestBodySpec signerPostSpec) {
        this.signerPostSpec = signerPostSpec;
    }

    /**
     * Create request to Signer Service
     *
     * @return request object
     */
    public SignContractAgreementRequestTO generateRequest() {
        // todo: change it to real values (and input params)
        SignContractAgreementRequestTO requestTO = new SignContractAgreementRequestTO();
        requestTO.setContexts(Arrays.asList(
                "https://w3id.org/security/suites/jws-2020/v1",
                "https://schema.org"
        ));
        requestTO.setFormat("ldp_vc");
        requestTO.setGroup("");
        requestTO.setKey("edkey");
        requestTO.setNamespace("transit");
        requestTO.setSignatureType("ed25519signature2020");
        requestTO.setStatus(true);

        Map<String, String> subjectData = new ConcurrentHashMap<>();
        subjectData.put("hello", "world2");

        requestTO.setCredentialSubjectData(subjectData);
        return requestTO;
    }

    /**
     * Send prepared request to Signer Service
     * In real case scenario analyze response and perform retry if needed
     *
     * @param requestTO - prepared request
     * @return Signer Service response as String
     */
    public String sendToSigner(SignContractAgreementRequestTO requestTO) {
        log.info("Prepared request: POST {}{}", signerServiceHost, SIGN_CONTRACT_ENDPOINT);
        LogUtils.logPayload(log, LogUtils.PayloadType.REQUEST, requestTO);
        WebClient.RequestHeadersSpec<?> headersSpec = signerPostSpec.body(BodyInserters.fromValue(requestTO));
        String stringResponse = prepareResponse(headersSpec, String.class);
        log.debug("Signer response:");
        log.debug(stringResponse);
        return stringResponse;
    }

    private <T> T prepareResponse(WebClient.RequestHeadersSpec<?> headersSpec, Class<T> responseClass) {
        WebClient.ResponseSpec response = headersSpec.retrieve();
        return response
                .onStatus(HttpStatusCode::isError,
                        responseError -> {
                            throw new ResponseStatusSingleException(
                                    responseError.statusCode(),
                                    responseError.bodyToMono(String.class).block());
                        })
                .bodyToMono(responseClass).block();
    }
}
