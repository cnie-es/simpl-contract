package eu.europa.ec.simpl.contracts.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class LocalExceptionHandlerTest {

    @Test
    void testHandlerMethodValidationException(){

        var handler = new LocalExceptionHandler();
        var input = mock(HandlerMethodValidationException.class);

        ResponseEntity<ProblemDetail> response = handler.handlePathVariableValidationMismatchException(input);

        assertNotNull(response, "Response is null");
        assertNotNull(response.getBody(), "Body is null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be BAD_REQUEST");
    }
}
