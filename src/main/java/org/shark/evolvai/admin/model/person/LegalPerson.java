package org.shark.evolvai.admin.model.person;

import jakarta.persistence.*;
import lombok.*;
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
