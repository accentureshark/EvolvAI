package org.shark.evolvai.admin.model.person;

import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.common.Person;
import org.shark.evolvai.admin.model.organization.AreaAssignment;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("NATURAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class NaturalPerson extends Person {

    private String firstName;

    private String lastName;

    private String nationalId;

    private LocalDate birthDate;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AreaAssignment> areaAssignments;

}
