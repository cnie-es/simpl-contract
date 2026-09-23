package eu.europa.ec.simpl.contracts.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.contracts.transfer.dto.CatalogRecordDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@Slf4j
public class ContractHumanReadableRenderer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String render(String json, String contractNegotiationId) throws JsonProcessingException {

        final var catalogRecord = parseCatalogRecord(json);

        // Header
        final var issuanceDate = catalogRecord.issuanceDate();

        // Provider
        final var providerName =
                catalogRecord.credentialSubject()
                        .providerInformation()
                        .providedBy();

        final var providerContact =
                catalogRecord.credentialSubject()
                        .providerInformation()
                        .contact();

        // Resource
        final var resourceId =
                catalogRecord.credentialSubject().id();

        final var resourceName =
                catalogRecord.credentialSubject()
                        .generalServiceProperties()
                        .name();

        final var resourceDescription =
                catalogRecord.credentialSubject()
                        .generalServiceProperties()
                        .description();

        final var offeringType =
                catalogRecord.credentialSubject()
                        .generalServiceProperties()
                        .offeringType();

        // Pricing
        final var price =
                catalogRecord.credentialSubject()
                        .offeringPrice()
                        .price()
                        .value();

        final var currency =
                catalogRecord.credentialSubject()
                        .offeringPrice()
                        .currency();

        // Policies
        final var usagePolicyHtml =
                renderPolicy(catalogRecord.credentialSubject()
                        .servicePolicy()
                        .usagePolicy());

        final var accessPolicyHtml =
                renderPolicy(catalogRecord.credentialSubject()
                        .servicePolicy()
                        .accessPolicy());

        // HTML output
        return """
                <h2>Header</h2>
                <p>Contract Negotiation ID: %s</p>
                <p>Generation Date: %s</p>
                <h2>Provider</h2>
                <p>Name: %s</p>
                <p>Contact: %s</p>
                <h2>Resource</h2>
                <p>ID: %s</p>
                <p>Name: %s</p>
                <p>Description: %s</p>
                <p>Offering Type: %s</p>
                <h2>Usage Policy</h2>
                %s
                <h2>Access Policy</h2>
                %s
                <h2>Pricing</h2>
                <p>Price: %s</p>
                <p>Currency: %s</p>""".formatted(
                contractNegotiationId,
                issuanceDate,
                providerName,
                providerContact,
                resourceId,
                resourceName,
                resourceDescription,
                offeringType,
                usagePolicyHtml,
                accessPolicyHtml,
                price,
                currency
        );
    }

    private static CatalogRecordDto parseCatalogRecord(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, CatalogRecordDto.class);
    }

    private static String renderPolicy(String embeddedPolicyJson) {
        if (embeddedPolicyJson == null || embeddedPolicyJson.isBlank()) {
            return "<p>No policy defined.</p>";
        }

        try {
            return renderPolicyInternal(embeddedPolicyJson);
        } catch (JsonProcessingException e) {
            log.error("Invalid policy format", e);
            return "<p>Invalid policy format</p>";
        }
    }

    private static String renderPolicyInternal(String embeddedPolicyJson)
            throws JsonProcessingException {

        final var policyNode = OBJECT_MAPPER.readTree(embeddedPolicyJson);
        final var permissions = policyNode.path("permission");

        if (!permissions.isArray()) {
            return "<p>No permissions found.</p>";
        }

        final var html = new StringBuilder(256);
        html.append("<ul>");

        for (final var permission : permissions) {
            for (final var action : permission.path("action")) {
                html.append("<li>Action: ")
                        .append(HtmlUtils.htmlEscape(action.asText()))
                        .append("</li>");
            }
        }

        html.append("</ul>");
        return html.toString();
    }
}

