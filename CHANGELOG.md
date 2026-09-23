## 2.9.0-edval (2026-09-10)

> Derivative work by the **EDNEL-RIOJA** project team for **CNIE-ES**, based on the upstream
> SIMPL-CONTRACT `v2.9.0` (commit `24ad51b`). Modified between **2026-06-10 and 2026-09-10**, licensed under EUPL-1.2 like
> the original work. See [NOTICE.EDNEL.md](NOTICE.EDNEL.md) for the full modification notice.

### Added (2026-06-10 → 2026-06-18)

- Manual **payment approval** for paid offerings on the provider side: a paid offering
  (catalogue `simpl:price` > 0) is parked in the new `PENDING_PAYMENT` status instead of being
  signed automatically, and is only signed once the provider confirms the external payment.
  Opt-in via `PAYMENT_APPROVAL_ENABLED`; free offerings keep the original behaviour
  (`PaymentApprovalService`, `ContractSigningService`, `SignContractRequestConsumer`,
  `ContractAgreementController`).
- Automatic rejection of agreements left unconfirmed for longer than
  `PAYMENT_APPROVAL_EXPIRATION_DAYS` (default 7), swept every
  `PAYMENT_APPROVAL_CHECK_INTERVAL_MS` (default 1 h).
- Liquibase changeset `v003/001_add_pending_payment_since.yaml` adding `pending_payment_since` to
  the contract agreement.
- Feature documentation: `docs/PAYMENT_APPROVAL.md` and `docs/payment-approval-flow.md`.
- GitHub Actions pipeline building and publishing the container image to
  `ghcr.io/ednel-rioja/k8s-images/contract` (2026-06-18).

### Changed (2026-06-11 → 2026-09-10)

- `pom.xml`: project version pinned to `2.9.0-edval` (upstream used `2.9.0-SNAPSHOT`) and artifact
  name marked as the EDNEL fork; `licenses`, `scm` and `url` metadata filled in (2026-09-02).
- Payment type resolved from the EDC negotiation payload; Helm chart and application configuration
  extended with the `paymentApproval` settings.
- `pipeline.variables.sh`: `PROJECT_VERSION_NUMBER` aligned with the fork version `2.9.0-edval`,
  which the file still declared as the upstream `2.9.0` (2026-09-10).
- Helm chart made installable outside the upstream GitLab pipeline (2026-09-10). `Chart.yaml` and
  `values.yaml` carried unsubstituted `${PROJECT_RELEASE_VERSION}` and `${CI_REGISTRY_IMAGE}`
  placeholders, which that pipeline replaced and which made the chart fail to load anywhere else
  (`helm lint` could not even parse it). The chart version and `appVersion` now hold the real
  version, `deployment.image.artifact` defaults to the published image of this fork and
  `deployment.image.tag` defaults to `.Chart.AppVersion`, both overridable at install time.
- `deployment.image.pullSecrets` added to the chart, empty by default, since the published image is
  private and the deployment previously had no way to reference pull credentials (2026-09-10).
- The pipeline now tags each image with the project version as well as the commit SHA, so that the
  tag the chart resolves to by default exists in the registry, lints the chart before building, and
  fails if the POM version and the chart `appVersion` ever drift apart (2026-09-10).

### Licence compliance (2026-09-02 → 2026-09-10)

Notices required by Art. 5 of the EUPL-1.2 (Attribution right, Provision of Source Code) for this
derivative work:

- `NOTICE.EDNEL.md`: modification notice stating that the work has been modified, by whom, when and
  what was changed, with the repository where the complete corresponding source code is available.
- `README.md`: prominent notice at the top of the file identifying this repository as a modified
  version of SIMPL-CONTRACT, plus a Licence section.
- `LICENSE`: the full official text of the EUPL-1.2 is now reproduced in the file, which previously
  only linked to it, so that a copy of the Licence travels with every copy of the Work. The
  original SIMPL heading and credits line are kept intact.
- `Dockerfile`: OCI image labels (`licenses`, `source`, `version`, `vendor`, `description`) and
  `LICENSE`, `NOTICE` and `NOTICE.EDNEL.md` copied into `/licenses/`, so the notices and the
  pointer to the source code travel with the published container image.
- `.github/workflows/build-and-push-image.yaml`: the container image namespace is derived from the
  repository owner instead of being hard-coded, so that the published image and the source code it
  is built from always live in the same organisation, and moving the project to its publishing
  organisation does not leave the pipeline pushing to a namespace this project no longer owns.
- The repository that the notices point to as the source of the complete corresponding source code
  is `https://github.com/cnie-es/simpl-contract` (2026-09-10). It is named in `NOTICE.EDNEL.md`,
  `README.md`, the `scm` and `url` metadata of `pom.xml` and the `org.opencontainers.image.source`
  label of the `Dockerfile`.
- `pom.xml`: the project coordinates move from `eu.europa.ec.simpl:CONTRACTS` to
  `es.cnie.simpl:simpl-contracts`, so that a modified artifact is not identified under a namespace
  belonging to the licensor (Art. 5, Legal Protection) — the POM embedded in the jar, and any SBOM
  derived from the image, now attribute the component to CNIE-ES instead of to the European
  Commission. The `eu.europa.ec.simpl:COMMON` dependency keeps its own coordinates, which correctly
  identify a third-party component, and the Java package `eu.europa.ec.simpl.contracts` is
  deliberately left unchanged so that the diff against the upstream baseline stays auditable.
  The upstream `distributionManagement`, which deployed to the Maven registry of the Commission
  GitLab project, is removed so that this fork cannot publish a modified artifact into the upstream
  namespace; the `repositories` entries stay, as they only resolve dependencies.


## 2.0.6 (2025-09-03)

### changed (5 changes)

- [[SIMPL-15093](https://jira.simplprogramme.eu/browse/SIMPL-15093) Remove...](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/commit/66f6b17eff70ce62313b81a327c2d98bf0878864) ([merge request](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/merge_requests/114))
- [[SIMPL-15093](https://jira.simplprogramme.eu/browse/SIMPL-15093) Remove...](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/commit/752f7bb20e31f02d7bd0535f0c8ca2247e6c5154) ([merge request](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/merge_requests/113))
- [[SIMPL-15093](https://jira.simplprogramme.eu/browse/SIMPL-15093) Remove...](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/commit/a9b8c107071cbeffbcfe8d834ac47a4d4b07f234) ([merge request](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/merge_requests/112))
- [[SIMPL-15093](https://jira.simplprogramme.eu/browse/SIMPL-15093) Remove...](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/commit/fb5141d6d94b597467244a12b1ff78f8edec7df8) ([merge request](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/merge_requests/111))
- [[SIMPL-15093](https://jira.simplprogramme.eu/browse/SIMPL-15093) Remove...](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/commit/4105fd81c5981e3ca4696b92de2f743510710e75) ([merge request](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/merge_requests/108))



## 2.0.3 (2025-07-23)

No changes.


## 2.0.2 (2025-07-14)

No changes.


## 2.0.1 (2025-06-24)

No changes.


## 2.0.0 (2025-05-29)

No changes.


## 1.0.4 (2025-05-12)

No changes.


## 1.0.3 (2025-04-18)

No changes.


## 1.0.2 (2025-03-28)

No changes.


# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2025-02-02

### Changed
- SIMPL-9162 Make kafka topics configurable
- java version in dockerfile + logs (use contractAgreementId everywhere)

## [1.0.0] - 2024-12-20

### Changed
- (SIMPL-8552) Fix sonar issue

## [0.0.4] - 202?-??-??

### Changed
- (SIMPL-8135) Fix fortify vulnerabilities; vulnerable dependencies handled
- (SIMPL-8864) Adjust vault integration in contract helm

### Added
- (SIMPL-8359) New GET endpoint for contract file + changelog format

### Changed
- (SIMPL-8157) Change package names and error handler
