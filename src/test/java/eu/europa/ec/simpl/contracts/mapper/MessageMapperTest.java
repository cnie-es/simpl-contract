package eu.europa.ec.simpl.contracts.mapper;

import eu.europa.ec.simpl.common.exceptions.ResponseStatusSingleException;
import eu.europa.ec.simpl.contracts.events.ContractAgreementRequestEvent;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static eu.europa.ec.simpl.contracts.transfer.Mode.PROVIDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MessageMapperTest {

    private final MessageMapper messageMapper = new MessageMapper();

    @Test
    void testConvertEventIntoStringMessage() {

        var eventToConvert = new ContractAgreementRequestEvent();
        eventToConvert.setMode(PROVIDER);
        eventToConvert.setContractDefinitionId("test_1234");

        var result = messageMapper.convertEventIntoStringMessage(eventToConvert);
        assertInstanceOf(String.class, result, "Convert event to String should return a string");
        assertTrue(result.contains("test_1234"), "Converted string should contain ContractDefinitionId");
        assertTrue(result.contains("PROVIDER"), "Converted string should contain Mode");
    }

    @ParameterizedTest
    @MethodSource("malformedKafkaMessages")
    void testMalformedMessageMapping(String malformedMessage, String expectedMessage, String invalidProperty) {

        ValidationException result = assertThrows(ValidationException.class,
                () -> messageMapper.createEvent(malformedMessage, ContractAgreementRequestEvent.class),
                "Should throw a ValidationException");

        assertTrue(result.getMessage().contains(expectedMessage)
                        && result.getMessage().contains(invalidProperty),
                "Validation result does not contain expected invalid property: "
                        + expectedMessage + " " + invalidProperty);
    }

    static Stream<Arguments> malformedKafkaMessages() {

        var malformedMessage1 = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "definition-456",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {}
                }
                """;

        var malformedMessage2 = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": ".............................too long ID ...............................................................................",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {
                    "contractNegotiationId": "1234"
                  }
                }
                """;

        var malformedMessage3 = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "definition-456",
                  "mode": "CONSUMER"
                }
                """;

        var malformedMessage4 = "{}";

        return Stream.of( ///
                arguments(malformedMessage1, "must not be null", "contractAgreementCreateTO.contractNegotiationId"),
                arguments(malformedMessage2, "contractDefinitionId may contain only alphanumeric characters and colons", "contractDefinitionId"),
                arguments(malformedMessage3, "must not be null", "contractAgreementCreateTO"),
                arguments(malformedMessage4, "must not be null", "mode"));
    }

    @Test
    void testValidMessageMapping() {
        var validMessage = """
                {
                    "contractAgreementId": "a01daaa6-4c6c-41fe-bf4c-41f6612e5c6e",
                    "contractDefinitionId": "06556cf6-5d05-4516-aa9b-3704cc7c12af",
                    "mode": "PROVIDER",
                    "contractAgreementCreateTO": {
                        "contractNegotiationId": "1",
                        "assetId": "2",
                        "providerId": "3",
                        "consumerId": "4",
                        "contractOfferId": "5"
                    }
                }
                """;

        var result = messageMapper.createEvent(validMessage, ContractAgreementRequestEvent.class);

        assertInstanceOf(ContractAgreementRequestEvent.class, result, "Validation failed");
        assertEquals(UUID.fromString("a01daaa6-4c6c-41fe-bf4c-41f6612e5c6e"), result.getContractAgreementId(),
                "Wrong contractAgreementId");
        assertEquals("06556cf6-5d05-4516-aa9b-3704cc7c12af", result.getContractDefinitionId(),
                "Wrong contractDefinitionId");
        assertEquals(PROVIDER, result.getMode(), "Wrong mode");
        assertNotNull(result.getContractAgreementCreateTO(), "No contractAgreementCreateTO");
        assertEquals("1", result.getContractAgreementCreateTO().getContractNegotiationId(),
                "Wrong contractAgreementCreateTO.contractNegotiationId");
        assertEquals("2", result.getContractAgreementCreateTO().getAssetId(),
                "Wrong contractAgreementCreateTO.assetId");
        assertEquals("3", result.getContractAgreementCreateTO().getProviderId(),
                "Wrong contractAgreementCreateTO.providerId");
        assertEquals("4", result.getContractAgreementCreateTO().getConsumerId(),
                "Wrong contractAgreementCreateTO.consumerId");
        assertEquals("5", result.getContractAgreementCreateTO().getContractOfferId(),
                "Wrong contractAgreementCreateTO.contractOfferId");
    }


    @Test
    void testEmptyMessageMapping() {

        ResponseStatusSingleException result = assertThrows(ResponseStatusSingleException.class,
                () -> messageMapper.createEvent("", ContractAgreementRequestEvent.class),
                "Should throw a ResponseStatusSingleException");

        assertTrue(result.getMessage().contains("Message JSON is empty or too long"),
                "Should throw a ResponseStatusSingleException");
    }

    @Test
    void testNullMessageMapping() {

        ResponseStatusSingleException result = assertThrows(ResponseStatusSingleException.class,
                () -> messageMapper.createEvent(null, ContractAgreementRequestEvent.class),
                "Should throw a ResponseStatusSingleException");

        assertTrue(result.getMessage().contains("Message JSON is empty or too long"),
                "Should throw a ResponseStatusSingleException");
    }

    @Test
    void testTooLongMessageMapping() {

        var veryLongMessage = "x".repeat(10001);

        ResponseStatusSingleException result = assertThrows(ResponseStatusSingleException.class,
                () -> messageMapper.createEvent(veryLongMessage, ContractAgreementRequestEvent.class),
                "Should throw a ResponseStatusSingleException");

        assertTrue(result.getMessage().contains("Message JSON is empty or too long"),
                "Should throw a ResponseStatusSingleException");
    }

    @Test
    void testTooDeepJSONTreeMessageMapping() {

        var tooDeepJSONTreeMessage = """
                {
                  "level1": {
                    "level2": {
                      "level3": {
                        "level4": {
                          "level5": {
                            "test": "deep value"
                          }
                        }
                      }
                    }
                  }
                }
                """;


        ResponseStatusSingleException result = assertThrows(ResponseStatusSingleException.class,
                () -> messageMapper.createEvent(tooDeepJSONTreeMessage, ContractAgreementRequestEvent.class),
                "Should throw a ResponseStatusSingleException");

        assertTrue(result.getMessage().contains("Message JSON too deeply nested"),
                "Should throw a ResponseStatusSingleException");
    }

    @Test
    void testDuplicationsMessageMapping() {
        var messageWithDuplications = """
                {
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractAgreementId": "123e4567-e89b-12d3-a456-426614174000",
                  "contractDefinitionId": "123e4567-e89b-12d3-a456-426614174000",
                  "mode": "CONSUMER",
                  "contractAgreementCreateTO": {
                    "contractNegotiationId": "1234"
                  }
                }
                """;

        ResponseStatusSingleException result = assertThrows(ResponseStatusSingleException.class,
                () -> messageMapper.createEvent(messageWithDuplications, ContractAgreementRequestEvent.class),
                "Should throw a ResponseStatusSingleException");

        assertTrue(result.getMessage().contains("Failed to deserialize JSON message"),
                "Should throw a ResponseStatusSingleException");
    }
}
