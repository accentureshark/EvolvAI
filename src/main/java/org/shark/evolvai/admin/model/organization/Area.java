package org.shark.evolvai.admin.model.organization;

import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.person.NaturalPerson;


import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "area", schema = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AreaAssignment> assignments;


}
