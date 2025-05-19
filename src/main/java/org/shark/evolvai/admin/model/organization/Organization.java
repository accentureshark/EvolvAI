package org.shark.evolvai.admin.model.organization;

import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.person.LegalPerson;

import java.util.UUID;

@Entity

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends LegalPerson {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;
}
