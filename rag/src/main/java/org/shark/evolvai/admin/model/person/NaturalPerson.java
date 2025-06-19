package org.shark.evolvai.admin.model.person;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.shark.evolvai.admin.model.common.Person;
import org.shark.evolvai.admin.model.organization.AreaAssignment;

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
