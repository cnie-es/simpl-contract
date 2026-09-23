package eu.europa.ec.simpl.contracts.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.common.exceptions.ResponseStatusSingleException;
import eu.europa.ec.simpl.contracts.events.ContractEvent;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    private static final int SECURE_MESSAGE_SIZE = 10000;

    private static final int SECURE_JSON_TREE_DEPTH = 5;

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T extends ContractEvent> T createEvent(String message, Class<T> clazz) {

        initialMessageCheck(message);
        try {
            treeDepthCheck(message);
            objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            final var mapped = objectMapper.readValue(message, clazz);
            validate(mapped);
            return mapped;
        } catch (JsonProcessingException e) {
            throw new ResponseStatusSingleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deserialize JSON message", e);
        }
    }

    public <T extends ContractEvent> String convertEventIntoStringMessage(T event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusSingleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to serialize JSON message", e);
        }
    }

    private static void initialMessageCheck(String message) {

        if (StringUtils.isBlank(message) || message.length() > SECURE_MESSAGE_SIZE) {
            throw new ResponseStatusSingleException(HttpStatus.BAD_REQUEST, "Message JSON is empty or too long");
        }
    }

    private static void treeDepthCheck(String message) throws JsonProcessingException {

        if (getJsonTreeDepth(objectMapper.readTree(message)) > SECURE_JSON_TREE_DEPTH) {
            throw new ResponseStatusSingleException(HttpStatus.BAD_REQUEST, "Message JSON too deeply nested");
        }
    }

    private static int getJsonTreeDepth(JsonNode node) {

        if (node == null || node.isValueNode()) {
            return 1;
        }
        var max = 0;
        for (final var child : node) {
            max = Math.max(max, getJsonTreeDepth(child));
        }
        return max + 1;
    }

    private static <T extends ContractEvent> void validate(T mapped) {

        final var validationResult = validator.validate(mapped);
        if (!validationResult.isEmpty()) {
            throw new ValidationException("Message validation failed: " + validationResult);
        }
    }
}
