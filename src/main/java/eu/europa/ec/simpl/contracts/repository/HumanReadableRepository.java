package eu.europa.ec.simpl.contracts.repository;

import eu.europa.ec.simpl.contracts.entity.HumanReadable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanReadableRepository extends JpaRepository<HumanReadable, String> {

}
