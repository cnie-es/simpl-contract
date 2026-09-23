package eu.europa.ec.simpl.contracts.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Sql("/database/ContractAgreementServiceConfirmationData.sql")
@Import(DocumentHashService.class)
class DocumentHashServiceTest {

    private static final String TEST_HASH = "{sha256}d286c8606c7c2b3f99063b3770d6c0a5cf419ee8bf3863356622a79bf1b55d31";

    @Autowired
    private DocumentHashService hashService;

    @Test
    void testCalculateHash() {

        var testString = "12345 some test";
        var result = hashService.calculateHash(testString.getBytes(StandardCharsets.UTF_8));
        assertEquals(TEST_HASH, result, "Unexpected hash result");
    }

    @Test
    void testCalculateHashEquity() {

        var testString = "12345 some test";
        var testString2 = "12345 some test";
        var result = hashService.calculateHash(testString.getBytes(StandardCharsets.UTF_8));
        var result2 = hashService.calculateHash(testString2.getBytes(StandardCharsets.UTF_8));
        assertEquals(result, result2, "Hashes for the same input should be equal");
    }

    @Test
    void testSaveToDatabase() {

        hashService.save("1", TEST_HASH);
        var databaseEntry = hashService.findById("1");
        assertNotNull(databaseEntry, "Human readable entity is null");
        assertEquals(TEST_HASH, databaseEntry.getRenderHash(), "Unexpected hash result");
    }

    @Test
    void testFindById() {

        var databaseEntry = hashService.findById("111");
        assertNotNull(databaseEntry, "Human readable entity is null");
        assertEquals("111", databaseEntry.getContractNegotiationId(), "Should be equal");
        assertEquals(TEST_HASH, databaseEntry.getRenderHash(), "Unexpected hash result");
    }

    @Test
    void testHashStringDocument() {

        var testString = "12345 some test";
        var result = hashService.calculateHash(testString);
        assertEquals(TEST_HASH, result, "Unexpected hash result");
    }
}
