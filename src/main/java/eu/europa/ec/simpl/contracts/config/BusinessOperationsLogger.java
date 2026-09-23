package eu.europa.ec.simpl.contracts.config;

import eu.europa.ec.simpl.common_logging.types.StructuredLogMessageBuilder;
import eu.europa.ec.simpl.contracts.types.BusinessOperations;
import eu.europa.ec.simpl.common_logging.levels.BusinessLevel;

import eu.europa.ec.simpl.common_logging.types.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static eu.europa.ec.simpl.common_logging.MessageBuilder.buildMessage;

@Component
@Slf4j
public final class BusinessOperationsLogger {

    private static final Logger LOGGER = LogManager.getLogger(BusinessOperationsLogger.class);

    private BusinessOperationsLogger() {
    }

    public static void log(BusinessOperations businessOperation, MessageType messageType, UUID id, String message) {

        final var logMessage = buildMessage(StructuredLogMessageBuilder.builder()
                .origin("eu.europa.ec.simpl.contracts")
                .destination("eu.europa.ec.simpl.contracts")
                .businessOperation(businessOperation.description())
                .messageType(messageType)
                .correlationId(id.toString())
                .msg(message + id)
                .build());

        LOGGER.log(BusinessLevel.BUSINESS, logMessage);
    }
}
