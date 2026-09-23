# Proceso de contratación de ofertas de pago (Payment Approval)

Este documento describe el flujo completo de contratación cuando una oferta es **de pago**, con la
retención manual de la firma hasta que el provider confirma que el pago se ha recibido por un medio
externo. Las ofertas **gratuitas** no se ven afectadas y se siguen firmando automáticamente.

> La feature es **opt-in** y solo aplica en el lado **provider**.

## 1. Configuración

Variables del despliegue del `contract` del provider:

| Variable | Defecto | Descripción |
|---|---|---|
| `PAYMENT_APPROVAL_ENABLED` | `false` | Activa la retención de ofertas de pago. Debe ser `true` (con `MODE=PROVIDER`). |
| `PAYMENT_APPROVAL_EXPIRATION_DAYS` | `7` | Días máximos que un acuerdo puede esperar confirmación antes de auto-rechazarse. |
| `PAYMENT_APPROVAL_CHECK_INTERVAL_MS` | `3600000` | Frecuencia (ms) del barrido que expira acuerdos pendientes. |

Si `PAYMENT_APPROVAL_ENABLED=false`, **todas** las ofertas (también las de pago) se firman
automáticamente.

## 2. De dónde sale el precio (`priceType`)

El `contract` **no** consulta el catálogo federado para saber si una oferta es de pago. El dato viaja
en la propia petición de firma:

1. Al **publicar la oferta**, el `EDCConnectorAdapter` escribe el precio en las *properties* del asset
   del EDC (`offer.priceType` = `free`/`commercial`, y `simpl:priceType`).
2. Al alcanzar el estado de firma, la extensión `ContractAgreementListener` (en `simpl-edc`) **resuelve
   `priceType` desde el asset** (vía `AssetIndex`) y lo incluye en el cuerpo de la llamada a
   `contract` (`POST /contract/v1/credentials/agreements/{id}/definitions/{def}/issue`).
3. `contract` recibe `priceType` dentro del `ContractAgreementCreateTO` del evento `SIGN_CONTRACT_REQUEST`.

**Fail-safe:** si `priceType` llega vacío/ausente, se trata como **de pago** (se retiene), para no liberar
nunca por error una oferta de pago.

## 3. Flujo completo (paid offering)

```mermaid
sequenceDiagram
    participant EDC as EDC + contractmanager (simpl-edc)
    participant K as Kafka
    participant C as contract
    participant UI as Provider (UI / API)
    participant CONS as Consumer

    EDC->>C: POST /credentials/agreements/{id}/definitions/{def}/issue (incluye priceType)
    C->>K: SIGN_CONTRACT_REQUEST (ContractAgreementCreateTO con priceType)
    K->>C: consume SIGN_CONTRACT_REQUEST
    Note over C: shouldHoldForPayment(priceType)
    alt priceType == "free"
        C->>C: signAndPublishResponse() (firma automática)
    else de pago (o priceType ausente → fail-safe)
        C->>C: markPendingPayment() → estado PENDING_PAYMENT
        Note over C,CONS: La negociación queda pendiente; el consumidor NO puede consumir todavía
    end

    UI->>C: GET /agreements/pending-payment
    UI->>C: POST /agreements/{id}/payment/confirm
    C->>C: signAndPublishResponse()
    C->>K: SIGN_CONTRACT_RESPONSE
    C->>EDC: notifyContractSigned()
    EDC->>C: STATUS_UPDATE (FINALIZED)
    Note over CONS: Activo disponible para el consumidor
```

### Estados del acuerdo

- `PENDING_PAYMENT` — retenido esperando confirmación de pago.
- `FINALIZED` — firmado y confirmado; el consumidor puede consumir.
- `TERMINATED` — rechazado (pago no recibido) o expirado.

## 4. Acciones del provider sobre un acuerdo retenido

Todas las llamadas requieren la cabecera **`x-api-key`** = `API_KEY` del servicio. Base path:
`/contract/v1/agreements`.

```bash
# Variables
CONTRACT=http://contract-dataprovider:8080      # interno; o el host público del BE del provider
APIKEY="$API_KEY"                               # mismo x-api-key que valida el AuthenticationFilter

# 1) Listar las que esperan pago (devuelve ContractAgreementTO[] con su contractAgreementId)
curl -s -H "x-api-key: $APIKEY" \
  "$CONTRACT/contract/v1/agreements/pending-payment" | jq .

# 2) Confirmar el pago de un acuerdo concreto  → FIRMA y libera la negociación
curl -s -X POST -H "x-api-key: $APIKEY" \
  "$CONTRACT/contract/v1/agreements/324c2038-4d7c-4b96-810e-e9a5f7cfeb59/payment/confirm" | jq .

# 3) (alternativa) Rechazar el pago  → TERMINA la negociación
curl -s -X POST -H "x-api-key: $APIKEY" \
  "$CONTRACT/contract/v1/agreements/324c2038-4d7c-4b96-810e-e9a5f7cfeb59/payment/reject" | jq .
```

El `contractAgreementId` se obtiene del paso 1.

### Qué hace `confirm`

`confirmPayment` ejecuta `contractSigningService.signAndPublishResponse(...)` — **el mismo camino de
firma** que se ejecutaría en automático para una oferta gratuita. A partir de ahí:

```
confirm → signAndPublishResponse → SIGN_CONTRACT_RESPONSE → EDCConnector.notifyContractSigned
        → STATUS_UPDATE (FINALIZED) → activo disponible para el consumidor
```

### Qué hace `reject`

`rejectPayment` ejecuta `publishRejection(...)` y deja el acuerdo en `TERMINATED`.

## 5. Reglas y casos borde

- `confirm`/`reject` solo funcionan si el acuerdo está en `PENDING_PAYMENT`; en otro caso devuelven
  `BadArgumentException` ("is not awaiting payment confirmation").
- Si nadie confirma ni rechaza, el barrido `expireStalePendingPayments` auto-rechaza el acuerdo a los
  `PAYMENT_APPROVAL_EXPIRATION_DAYS` (7 por defecto), para no dejar negociaciones colgadas.
- Mientras está en `PENDING_PAYMENT`, la negociación del EDC permanece pendiente (sin agreement), así
  que el consumidor no puede consumir.

## 6. Referencias de código

- `contract`:
  - `consumer/SignContractRequestConsumer` — decide retener o firmar.
  - `service/PaymentApprovalService` — `shouldHoldForPayment(priceType)`, `confirmPayment`, `rejectPayment`,
    `expireStalePendingPayments`.
  - `controller/ContractAgreementController` — endpoints `pending-payment`, `payment/confirm`, `payment/reject`.
  - `transfer/ContractAgreementCreateTO` — campo `priceType`.
- `simpl-edc`:
  - `listener/ContractAgreementListener` — resuelve `priceType` del asset (`AssetIndex`) y lo añade al
    cuerpo de la llamada de firma.
  - `service/ContractSignCallbackEndpointExtension` — inyección del `AssetIndex`.
- `EDCConnectorAdapter`:
  - `service/RegistrationServiceImpl` — escribe `offer.priceType`/`simpl:priceType` en las properties del
    asset al registrar la oferta.
