package eu.europa.ec.simpl.contracts.mapper;

import eu.europa.ec.simpl.contracts.events.ContractAgreementRequestEvent;
import eu.europa.ec.simpl.contracts.events.ContractSearchRequestEvent;
import eu.europa.ec.simpl.contracts.events.CreateContractDataRequestEvent;
import eu.europa.ec.simpl.contracts.events.StatusUpdateRequestEvent;
import eu.europa.ec.simpl.contracts.transfer.ContractResponseTO;
import eu.europa.ec.simpl.contracts.transfer.ContractSearchConfirmationTO;
import eu.europa.ec.simpl.contracts.types.ContractAgreementStatusType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContractMapper {
    ContractMapper INSTANCE = Mappers.getMapper(ContractMapper.class);

    ContractResponseTO mapToTO(ContractAgreementRequestEvent event, ContractAgreementStatusType status);

    ContractResponseTO mapToTO(StatusUpdateRequestEvent event, ContractAgreementStatusType status);

    ContractResponseTO mapToTO(CreateContractDataRequestEvent event, ContractAgreementStatusType status);

    ContractSearchConfirmationTO mapToTO(ContractSearchRequestEvent event);
}
