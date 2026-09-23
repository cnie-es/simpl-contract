package eu.europa.ec.simpl.contracts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.simpl.contracts.mapper.ContractHumanReadableRenderer;
import eu.europa.ec.simpl.contracts.transfer.ContractDataResponseTO;
import eu.europa.ec.simpl.contracts.utils.WebServerUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractHumanReadableRendererTest {

    private ContractHumanReadableRenderer contractHumanReadableRenderer = new ContractHumanReadableRenderer();

    @Test
    void testHumanReadableGeneration() throws JsonProcessingException {

        var recordData = WebServerUtils.getFileContent("files/catalog-get-response.json");
        var result = contractHumanReadableRenderer.render(recordData, "negotiationId1");

        var expectedHtml = WebServerUtils.getFileContent("files/expected-human-readable.html");

        assertEquals(expectedHtml.stripTrailing(), result);
    }

    @Test
    void justTestResponseStructure() {
        var testResponse = new ContractDataResponseTO();
        testResponse.setHumanReadable("a");
        testResponse.setResponse("b");
        testResponse.setHash("c");
        testResponse.setContractNegotiationId("d");
        testResponse.setErrorMessage("x");
        testResponse.setErrorCode(12);
        assertEquals("a", testResponse.getHumanReadable());
        assertEquals("b", testResponse.getResponse());
        assertEquals("c", testResponse.getHash());
        assertEquals("d", testResponse.getContractNegotiationId());
        assertEquals(12, testResponse.getErrorCode());
        assertEquals("x", testResponse.getErrorMessage());
    }
}
