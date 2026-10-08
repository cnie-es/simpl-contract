FROM eclipse-temurin:21-jdk-alpine@sha256:cd87715a8d45cfaa42419207c64680234f62785c49055cccd20437b5c9018380

ARG IMAGE_REVISION="0000000000000000000000000000000000000000"
ARG IMAGE_CREATED="1970-01-01T00:00:00Z"

# EUPL-1.2 (Art. 5): this image ships a modified version of SIMPL-CONTRACT. The licence, the
# third-party notices and the modification notice travel with the image, and the label below points
# to the repository where the complete corresponding source code is freely available.
LABEL org.opencontainers.image.title="CONTRACTS (CNIE-ES fork)" \
      org.opencontainers.image.description="Modified version of SIMPL-CONTRACT v2.9.0 (upstream commit 24ad51b), modified by the EDNEL-RIOJA project team for CNIE-ES between 2026-06-10 and 2026-09-10. See /licenses/NOTICE.EDNEL.md." \
      org.opencontainers.image.version="2.9.1-edval" \
      org.opencontainers.image.vendor="CNIE-ES" \
      org.opencontainers.image.licenses="EUPL-1.2" \
      org.opencontainers.image.source="https://github.com/cnie-es/simpl-contract" \
      org.opencontainers.image.revision="${IMAGE_REVISION}" \
      org.opencontainers.image.created="${IMAGE_CREATED}"

COPY LICENSE NOTICE NOTICE.json NOTICE.EDNEL.md /licenses/

# Both artifacts come from release/build.sh: the jar it packages, and the OpenTelemetry agent
# it resolves from Maven Central against its published checksum. Nothing is fetched here.
COPY target/simpl-contracts.jar simpl-contracts.jar
COPY otel/elastic-otel-javaagent.jar /otel/elastic-otel-javaagent.jar

RUN mkdir -p /files && chmod 775 /files && chmod 775 /otel

RUN adduser -u 8877 -D dockeruser && chown -R dockeruser:dockeruser /files && chown -R dockeruser:dockeruser /otel
USER dockeruser
EXPOSE 8080

# Run with OpenTelemetry agent
ENV OTEL_SERVICE_NAME=simpl-contracts
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://collector.common01.dev.simpl-europe.eu/
ENV OTEL_LOGS_EXPORTER="none"

CMD ["java", "-javaagent:/otel/elastic-otel-javaagent.jar", "-jar", "simpl-contracts.jar"]
