package com.data.entity;

import com.data.entity.enums.Gender;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
@Entity
@Table(name = "candidate")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String name;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(15)")
    private String phone;

    @Column(nullable = false)
    private int experience = 0;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MALE', 'FEMALE', 'OTHER')")
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dob;

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Account account;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "candidate_technology",
            joinColumns = @JoinColumn(name = "candidateId"),
            inverseJoinColumns = @JoinColumn(name = "technologyId")
    )
    private List<Technology> technologies;

    // Phương thức helper để lấy email
    @Transient
    public String getEmail() {
        return account != null ? account.getEmail() : null;
    }

    // Phương thức helper để tính tuổi
    @Transient
    public Integer getAge() {
        if (dob == null) return null;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // Phương thức helper để kiểm tra trạng thái
    @Transient
    public boolean isAccountActive() {
        return account != null && account.getStatus() != null &&
                "ACTIVE".equals(account.getStatus().name());
    }
}
