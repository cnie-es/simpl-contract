package eu.europa.ec.simpl.contracts.producer;

import eu.europa.ec.simpl.contracts.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static eu.europa.ec.simpl.contracts.kafka.KafkaTopic.SIGN_CONTRACT_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageProducerTest {

    private static final Long TIMEOUT = 5000L;

    private MessageProducer producer;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        producer = new MessageProducer(new MessageMapper(), kafkaTemplate);
        ReflectionTestUtils.setField(producer, "signContractResponseTopic", "sign-contract-resp");
        ReflectionTestUtils.setField(producer, "signContractRequestTopic", "sign-contract-req");
        ReflectionTestUtils.setField(producer, "statusUpdateTopic", "status-update");
    }

    @Test
    void testSendMessage() {

        SendResult<String, String> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, String>> successfulFuture = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(),any())).thenReturn(successfulFuture);

        var result = producer.sendMessage(SIGN_CONTRACT_RESPONSE, null);

        assertTrue(result, "Result should be successful");
        verify(kafkaTemplate, timeout(TIMEOUT).times(1)).send("sign-contract-resp", "null");
    }

    @Test
    void testKafkaError(){

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka failed test"));
        when(kafkaTemplate.send(any(),any())).thenReturn(failedFuture);

        var result = producer.sendMessage(SIGN_CONTRACT_RESPONSE, null);

        assertFalse(result, "Result should fail");
        verify(kafkaTemplate, timeout(TIMEOUT).times(1)).send("sign-contract-resp", "null");
    }
}
