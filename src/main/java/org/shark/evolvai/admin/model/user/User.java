package org.shark.evolvai.admin.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.person.NaturalPerson;
import org.shark.evolvai.admin.model.person.Role;

import java.util.UUID;

@Entity
@Table(name = "user", schema = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", unique = true)
    private NaturalPerson person;
}
