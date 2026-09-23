# Modification notice (EUPL 1.2, Art. 5)

**This is a modified version of SIMPL-CONTRACT. It is not the original work.**

The original work, SIMPL-CONTRACT, is part of the SIMPL programme (© European Union / SIMPL
Programme) and is licensed under the **European Union Public Licence v. 1.2 (EUPL-1.2)**. See
[LICENSE](LICENSE) for the licence of the work and [NOTICE](NOTICE) / [NOTICE.json](NOTICE.json)
for the third-party components included in it. All original copyright, licence and disclaimer
notices are kept intact and unmodified in this fork.

## Upstream baseline

| | |
|---|---|
| Original work | SIMPL-CONTRACT (`eu.europa.ec.simpl:CONTRACTS`) |
| Upstream repository | https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract |
| Baseline version | `v2.9.0` |
| Baseline commit | `24ad51b965b81c82e5b6ef24b1ce02ce2c195f18` (2026-03-05) |

## Modifications

| | |
|---|---|
| Modified by | EDNEL-RIOJA project team, for CNIE-ES |
| Public repository of this derivative work | https://github.com/cnie-es/simpl-contract |
| Version of this derivative work | `2.9.0-edval` |
| Dates of modification | **2026-06-10 to 2026-09-10** |

The modifications are licensed under the **EUPL-1.2**, the same licence as the original work.

The complete source code of this derivative work is available at the public repository above as a
vetted release snapshot, and will remain freely available there for as long as the Work is
distributed. The upstream repository and exact baseline commit are recorded above. Each release
snapshot includes `SBOM.cyclonedx.json`, which binds the upstream and work revisions, release tag,
public repository, snapshot hash and image digest. The distribution history contains release
snapshots rather than a copy of the upstream Git history, so the complete set of changes is the
diff between the baseline commit `24ad51b`, fetched from its authoritative upstream repository,
and the published snapshot. The tables below record what each file contributed. The published
container images (`ghcr.io/cnie-es/contract`) are built from that snapshot.

### Summary of the changes

Manual **payment approval** for paid offerings on the provider side: paid offerings are no longer
signed automatically but parked in status `PENDING_PAYMENT` until the provider confirms that the
consumer has paid by an external means, with automatic rejection after a configurable window.
The feature is opt-in (`PAYMENT_APPROVAL_ENABLED`, provider mode only); free offerings keep the
original behaviour. See [docs/PAYMENT_APPROVAL.md](docs/PAYMENT_APPROVAL.md) and
[docs/payment-approval-flow.md](docs/payment-approval-flow.md). A GitHub Actions pipeline that
builds and publishes the container image was also added.

A second, non-functional group of changes (2026-09-02 to 2026-09-10) adds the notices this licence
requires of a derivative work: this file, the notice at the top of [README.md](README.md), the
licence and source-code metadata in `pom.xml` and in the OCI labels of the `Dockerfile`, and the
reproduction of the full official licence text inside [LICENSE](LICENSE), which previously only
linked to it. See [CHANGELOG.md](CHANGELOG.md) for the itemised list.

### Files added

| File | Date |
|---|---|
| `.github/workflows/build-and-push-image.yaml` | 2026-06-18 (updated 2026-09-10) |
| `docs/PAYMENT_APPROVAL.md` | 2026-06-10 |
| `docs/payment-approval-flow.md` | 2026-06-18 |
| `src/main/java/eu/europa/ec/simpl/contracts/service/ContractSigningService.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/service/PaymentApprovalService.java` | 2026-06-18 |
| `src/main/resources/liquibase/v003/001_add_pending_payment_since.yaml` | 2026-06-10 |
| `NOTICE.EDNEL.md` (this file) | 2026-09-02, 2026-09-14 |

### Files modified

| File | Date |
|---|---|
| `.gitignore` | 2026-09-14 |
| `charts/Chart.yaml` | 2026-09-10 |
| `charts/README.md` | 2026-09-10 |
| `charts/templates/deployment.yaml` | 2026-06-10, 2026-09-10 |
| `charts/values.yaml` | 2026-06-10, 2026-09-10 |
| `pom.xml` | 2026-06-11, 2026-09-02, 2026-09-10, 2026-09-14 |
| `src/main/java/eu/europa/ec/simpl/contracts/SimplContractsApplication.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/consumer/SignContractRequestConsumer.java` | 2026-06-18 |
| `src/main/java/eu/europa/ec/simpl/contracts/controller/ContractAgreementController.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/entity/ContractAgreement.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/repository/ContractAgreementRepository.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/transfer/ContractAgreementCreateTO.java` | 2026-06-18 |
| `src/main/java/eu/europa/ec/simpl/contracts/transfer/ContractAgreementTO.java` | 2026-06-10 |
| `src/main/java/eu/europa/ec/simpl/contracts/types/ContractAgreementStatusType.java` | 2026-06-10 |
| `src/main/resources/application-local.yaml` | 2026-06-10 |
| `src/main/resources/application.yaml` | 2026-06-10 |
| `src/main/resources/liquibase/changelog-master.yaml` | 2026-06-10 |
| `pipeline.variables.sh` | 2026-09-10 |
| `Dockerfile` | 2026-09-02 |
| `README.md` | 2026-09-02 |
| `CHANGELOG.md` | 2026-06-18, 2026-09-10 |
| `LICENSE` | 2026-09-10 |

No file of the original work has been removed, and no copyright, licence or disclaimer notice of
the original work has been altered. The only change to [LICENSE](LICENSE) is the addition, below
the original heading and credits line, of the full official text of the EUPL-1.2 that the file
previously referenced only by hyperlink; nothing in the original file was removed or reworded.

The per-file diff for every change is obtainable with:

```
git diff 24ad51b965b81c82e5b6ef24b1ce02ce2c195f18..2.9.0-edval
```
