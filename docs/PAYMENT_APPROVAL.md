# Payment approval for paid offerings (provider side) — v2.9.0

## What it does

When enabled on the **provider** connector, the contract module no longer signs paid offerings
automatically. Instead:

- **Free offering** (catalogue `simpl:price` ≤ 0 or absent-as-free): signed automatically, as before.
- **Paid offering** (`simpl:price` > 0): the contract agreement is parked in status
  `PENDING_PAYMENT` and the underlying EDC negotiation stays *pending*. The provider must confirm —
  through the UI — that the consumer has paid by an external means before the contract is signed and
  the consumer is allowed to consume the asset.

The decision is based on the offering price, fetched from the catalogue at signing time using the
`assetId` + `contractDefinitionId`. If the price cannot be resolved, the offering is treated as
**paid** (fail-safe), so a paid asset is never released by mistake.

A parked agreement that is neither confirmed nor rejected is **automatically rejected** after a
configurable window (default **7 days**).

## Configuration (deployment)

Helm values (`contract` chart), under `deployment.paymentApproval`:

| Value | Env var | Default | Meaning |
|-------|---------|---------|---------|
| `enabled` | `PAYMENT_APPROVAL_ENABLED` | `false` | Turn the gate on. Only effective when `mode: PROVIDER`. |
| `expirationDays` | `PAYMENT_APPROVAL_EXPIRATION_DAYS` | `7` | Max days a paid offering may wait for confirmation. |
| `checkIntervalMs` | `PAYMENT_APPROVAL_CHECK_INTERVAL_MS` | `3600000` | How often the expiration sweep runs (ms). |

Off by default, so existing deployments behave exactly as today until enabled on the provider's
contract module.

## REST API for the frontend

Base path: `/contract/v1/agreements`. Authentication is the same as the rest of the contract
module's API — use the same scheme the UI already uses for the other `/contract/v1` endpoints.

### 1. List agreements awaiting payment confirmation

```
GET /contract/v1/agreements/pending-payment
```

Returns the agreements in `PENDING_PAYMENT`:

```json
[
  {
    "contractAgreementId": "0f0e...uuid",
    "contractDefinitionId": "120781c3-eafc-44b4-9054-a28e2528155b",
    "contractNegotiationId": "negotiation-id",
    "assetId": "5900b594-30f3-44e7-9b0b-53d96d575cey",
    "providerId": "did:web:provider...",
    "consumerId": "did:web:consumer...",
    "contractOfferId": "offer-id",
    "status": "PENDING_PAYMENT",
    "pendingPaymentSince": "2026-06-10T09:15:00",
    "consumerSignatureDate": "2026-06-10T09:15:00",
    "providerSignatureDate": null
  }
]
```

The UI can compute the **deadline** as `pendingPaymentSince + expirationDays`. After that the item is
auto-rejected and leaves this list (status becomes `TERMINATED`).

### 2. Confirm payment → sign the contract

```
POST /contract/v1/agreements/{contractAgreementId}/payment/confirm
```

Use after the operator has verified the external payment. The module signs the contract and notifies
the connector, which finalises the negotiation. Returns the updated agreement (`status` normally
`CREATED`, then `FINALIZED` once the connector confirms).

### 3. Reject payment → terminate the contract

```
POST /contract/v1/agreements/{contractAgreementId}/payment/reject
```

Use when the payment was not received / refused. The connector terminates the negotiation. Returns
the updated agreement (`status` `TERMINATED`).

### Errors

| HTTP | When |
|------|------|
| `404` | No agreement with that id. |
| `400` | The agreement is not in `PENDING_PAYMENT` (e.g. already confirmed, rejected or expired). |

## Suggested UI workflow

1. Poll `GET /pending-payment` to show the queue of paid offerings awaiting confirmation, with
   consumer, asset and the computed deadline.
2. The operator checks, out-of-band, that the consumer has paid.
3. **Confirm** → `POST .../payment/confirm`. The row disappears; the negotiation finalises and the
   consumer can consume.
4. **Reject** → `POST .../payment/reject`. The negotiation is terminated.
5. Items not actioned within `expirationDays` are auto-rejected; the UI should tolerate a pending row
   disappearing on its own and reflect the resulting `TERMINATED` state.

## Status lifecycle

```
INITIATED ─┬─(free)──────────────► CREATED ──► FINALIZED
           │
           └─(paid)► PENDING_PAYMENT ─┬─(confirm)─► CREATED ──► FINALIZED
                                      ├─(reject)──► TERMINATED
                                      └─(expiry)──► TERMINATED
```

## Note on this version (2.9.0)

In 2.9.0 the signer integration is the pre-VC API (`SignContractService.generateRequest()` +
`sendToSigner()`); there is no Verifiable Credential issuance (that was added in 3.x). The
payment-approval gate is wired around that signer call, so confirming a payment triggers the 2.9.0
signing path and the same `signed` callback to the connector.
