package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.contracts.entity.HumanReadable;
import eu.europa.ec.simpl.contracts.repository.HumanReadableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentHashService {

    private final HumanReadableRepository repository;

    public String calculateHash(byte[] document) {

        try {
            final var messageDigest = MessageDigest.getInstance("SHA-256");
            final var hash = messageDigest.digest(document);
            return "{sha256}" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException while generating hash", e);
        }
        return null;
    }

    public String calculateHash(String document) {

        return calculateHash(document.getBytes(StandardCharsets.UTF_8));
    }

    public HumanReadable save(String contractNegotiationID, String hashValue) {

        final var entity = new HumanReadable(contractNegotiationID, hashValue);
        return repository.save(entity);
    }

    public HumanReadable findById(String contractNegotiationID) {

        return repository.findById(contractNegotiationID).orElse(null);
    }
}
