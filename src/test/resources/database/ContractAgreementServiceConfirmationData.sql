delete from contract_agreements;

insert into contract_agreements (contract_agreement_id, contract_definition_id, provider_signature_date, consumer_signature_date, status)
values ('807fdb6b-a9de-430d-9bb2-c5909b4b2061', '807fdb6b-a9de-430d-9bb2-c5909b4b2064', '2023-07-04 13:37:15.561754', '2023-07-04 13:38:15.561754', 'FINALIZED');

insert into contract_agreements (contract_agreement_id, contract_definition_id, provider_signature_date, consumer_signature_date, status,
                                 contract_negotiation_id, asset_id, provider_id, consumer_id, contract_offer_id)
values ('807fdb6b-a9de-430d-9bb2-c5909b4b2062', '807fdb6b-a9de-430d-9bb2-c5909b4b2064', '2023-07-04 13:37:15.561754', '2023-07-04 13:38:15.561754', 'CREATED',
        'X1', 'X2', 'X3', 'X4', 'X5');

insert into contract_agreements (contract_agreement_id, contract_definition_id, provider_signature_date, consumer_signature_date, status)
values ('807fdb6b-a9de-430d-9bb2-c5909b4b2063', '807fdb6b-a9de-430d-9bb2-c5909b4b2064', '2023-07-04 13:37:15.561754', '2023-07-04 13:38:15.561754', 'CREATED');

insert into contract_agreements (contract_agreement_id, contract_definition_id, provider_signature_date, consumer_signature_date, status)
values ('807fdb6b-a9de-430d-9bb2-c5909b4b2064', '807fdb6b-a9de-430d-9bb2-c5909b4b2064', '2023-07-04 13:37:15.561754', '2023-07-04 13:38:15.561754', 'CREATED');

insert into contract_agreements (contract_agreement_id, contract_definition_id, provider_signature_date, consumer_signature_date, status)
values ('807fdb6b-a9de-430d-9bb2-c5909b4b2777', '807fdb6b-a9de-430d-9bb2-c5909b4b2777', '2023-07-04 13:37:15.561754', '2023-07-04 13:38:15.561754', 'TERMINATED');

delete from human_readable;

insert into human_readable(contract_negotiation_id, render_hash)
values ('111', '{sha256}d286c8606c7c2b3f99063b3770d6c0a5cf419ee8bf3863356622a79bf1b55d31');