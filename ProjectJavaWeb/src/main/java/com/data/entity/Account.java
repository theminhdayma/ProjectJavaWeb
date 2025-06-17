package com.data.entity;

import com.data.entity.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int accountId;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "candidate_id", referencedColumnName = "id", unique = true)
    private Candidate candidate;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ADMIN', 'CANDIDATE') DEFAULT 'CANDIDATE'")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'")
    private Status status;
}
