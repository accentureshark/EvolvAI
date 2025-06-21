package org.shark.evolvai.admin.model.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.shark.evolvai.admin.model.common.Person;

@Entity
@DiscriminatorValue("LEGAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class LegalPerson extends Person {

    private String businessName;

    private String taxId;

    private String registrationNumber;
}
