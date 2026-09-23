package eu.europa.ec.simpl.contracts.mapper;

import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import eu.europa.ec.simpl.contracts.transfer.ContractAgreementTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContractAgreementMapper {
    ContractAgreementMapper INSTANCE = Mappers.getMapper(ContractAgreementMapper.class);

    ContractAgreementTO mapToTO(ContractAgreement contractAgreement);
}
