package com.data.entity;


import com.data.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "recruitment_position")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(100)")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "DECIMAL(10, 2)")
    private Double minSalary;

    @Column(nullable = false, columnDefinition = "DECIMAL(10, 2)")
    private Double maxSalary;

    @Column(nullable = false , columnDefinition = "INT DEFAULT 0")
    private int minExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'")
    private Status status;

    @Column(nullable = false, columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDate createdDate;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate expiredDate;

    @ManyToMany
    @JoinTable(
            name = "recruitment_position_technology",
            joinColumns = @JoinColumn(name = "positionId"),
            inverseJoinColumns = @JoinColumn(name = "technologyId")
    )
    private Set<Technology> technologies;
}
