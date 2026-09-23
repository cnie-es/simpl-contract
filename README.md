# SIMPL-CONTRACT

> ⚠️ **Modified work — CNIE-ES fork.**
> This repository is **not** the original SIMPL-CONTRACT. It is a derivative work based on
> SIMPL-CONTRACT `v2.9.0` (commit `24ad51b`,
> [upstream](https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract)),
> modified by the EDNEL-RIOJA project team for CNIE-ES between **2026-06-10 and 2026-09-10**
> to add manual payment approval for paid offerings. Distributed as version `2.9.0-edval` under the **EUPL-1.2**, the same licence as
> the original work. Full details of what was changed and when:
> [NOTICE.EDNEL.md](NOTICE.EDNEL.md).

> **Purpose**: This service enhances contract management between DataSpace participants by supporting the storage, 
consultation, and updating of signed contracts. It extends the contract establishment process with additional 
negotiation capabilities. It also monitors and enforces contract-defined resource usage, triggering contract closure
and resource decommissioning when required.

---


## 📑 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [⚡ Quick Start](#-quick-start)
   - [Use as a Dependency](#use-as-dependency)
   - [Run Locally](#run-locally)
4. [Installation guide](#installation-guide)
5. [User guide](#user-guide)
6. [Testing](#testing)
7. [Contributing](#contributing)
8. [Contact & Support](#contact--support)

---

## Overview

This service provides following functionalities:
- Functionality extending the participants contract management to store, consult or update (as appropriate) the signed 
contracts established via DataSpace connectors interaction between consumer and provider participants at contract 
negotiation and signature.
- Functionality extending the participants contract management additional negotiation steps in the contract 
establishment. 
- Functionality reporting on and enforcing resources usage defined in contracts, including the trigger of contract 
closure, decommissioning of resources trigger. Monitoring functionality expected to contribute to this trigger 
and usage reporting.

## Prerequisites

```
Java 21+
Maven 3.9+
Access to EU GitLab Package Registry (for repo declared in POM file)
IDE with plugin Lombok enabled (IntelliJ/Eclipse/VS Code)
Enabled connectivity with Kafka cluster
Enabled connectivity with EDC Consumer/Provider Connector
Enabled connectivity with Security Vault
Access to Postgres database
```

---

## ⚡ Quick Start

## Use as dependency

This service is not intended to be used as a dependency. It should be deployed and run as a separate service.

---

## Run Locally

The instructions for running the application locally can be found in the following file 
→ [Installation Guide](documents/installation-guide/installation-guide.md)

---

## Installation Guide

The instructions for running the application locally can be found in the following file
→ [Installation Guide](documents/installation-guide/installation-guide.md)

The instructions for setting up configuration and deploy in Kubernetes cluster can be found in the following file 
→ [Deployment Guide](documents/deployment-guide/deployment-guide.md)

---

## User Guide

At the following link, you can find the guide that outlines the changes made in the latest version, 
including configuration updates, integrations with other systems, and new or modified functionalities, 
to facilitate the setup of the application within the target environment. 
→ [Upgrade Guide](documents/upgrade-guide/upgrade-guide.md)

---

## Testing

Testing is covered through the CI/CD pipeline associated with the GIT repository.
This pipeline automatically runs Unit Tests, SAST (Static Application Security Testing) using SonarQube, 
and security tests performed with Fortify.

---

## Contributing

At the following link, you can find all the information related to the delivery process management 
adopted for Simpl-Open across its various components.
[Release Management](https://confluence.simplprogramme.eu/display/SIMPL/2050+-+Release+mgnt)

---

## Contact & Support

- **Maintainers**: `Contract-Billing Team`
- **Issue Tracker**: https://code.europa.eu/simpl/simpl-open/development/contract-billing/contract/-/issues
- **Support**: cnect-simpl@ec.europa.eu

---


## Licence

The original work, SIMPL-CONTRACT, is © European Union / SIMPL Programme and is licensed under the
**European Union Public Licence v. 1.2 (EUPL-1.2)**, whose full official text is reproduced in
[LICENSE](LICENSE). Third-party components included in the product are listed in
[NOTICE](NOTICE) / [NOTICE.json](NOTICE.json).

This fork is a **modified version** of that work, distributed under the same licence. The
modification notice required by Art. 5 of the EUPL (what was modified, by whom and when) is in
[NOTICE.EDNEL.md](NOTICE.EDNEL.md). The complete corresponding source code, including the revision
history, is available at https://github.com/cnie-es/simpl-contract.

---
