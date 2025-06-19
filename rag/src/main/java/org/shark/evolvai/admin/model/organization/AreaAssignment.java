package org.shark.evolvai.admin.model.organization;


import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shark.evolvai.admin.model.person.NaturalPerson;

@Entity
@Table(name = "area_assignment", schema = "admin",
        uniqueConstraints = @UniqueConstraint(columnNames = {"area_id", "person_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private NaturalPerson person;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;

    @Column(name = "local_role")
    private String localRole; // e.g., "Team Lead", "Auditor", "Manager"
}
