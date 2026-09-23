package eu.europa.ec.simpl.contracts.types;

import org.junit.jupiter.api.Test;

import static eu.europa.ec.simpl.contracts.types.BusinessOperations.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessOperationsTest {

    @Test
    void testBusinessOperationsEnum() {

        assertEquals("ISSUE_CONTRACT", BP07_01.description(), "Incorrect description");
        assertEquals("CONTRACT_TERMINATE", BP07_06.description(), "Incorrect description");
        assertEquals("CONTRACT_FINALIZE", BP07_09.description(), "Incorrect description");

        assertEquals("BP07_01", BP07_01.toString(), "Incorrect enum toString");
        assertEquals("BP07_06", BP07_06.toString(),  "Incorrect enum toString");
        assertEquals("BP07_09", BP07_09.toString(),  "Incorrect enum toString");
    }
}
