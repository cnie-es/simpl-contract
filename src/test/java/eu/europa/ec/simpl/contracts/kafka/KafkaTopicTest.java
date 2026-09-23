package eu.europa.ec.simpl.contracts.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopicTest {

    @Test
    void testEnum() {
        assertEquals("SIGN_CONTRACT_REQUEST", KafkaTopic.SIGN_CONTRACT_REQUEST.name(), "Wrong enum value");
        assertEquals("SIGN_CONTRACT_RESPONSE", KafkaTopic.SIGN_CONTRACT_RESPONSE.name(), "Wrong enum value");
        assertEquals("STATUS_UPDATE", KafkaTopic.STATUS_UPDATE.name(), "Wrong enum value");
    }
}
