package eu.europa.ec.simpl.contracts.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "human_readable")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HumanReadable {

    @Id
    private String contractNegotiationId;

    private String renderHash;
}
