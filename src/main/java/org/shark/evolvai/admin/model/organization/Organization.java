package org.shark.evolvai.admin.model.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
