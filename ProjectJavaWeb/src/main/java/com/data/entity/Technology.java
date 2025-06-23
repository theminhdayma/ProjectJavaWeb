package com.data.entity;

import com.data.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "technology")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Technology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(100)")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'")
    private Status status;

    @ManyToMany(mappedBy = "technologies")
    private List<Candidate> candidates;

    @ManyToMany(mappedBy = "technologies")
    private List<RecruitmentPosition> positions;

    @Override
    public String toString() {
        return this.name;
    }
}
