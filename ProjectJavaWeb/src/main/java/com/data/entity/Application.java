package com.data.entity;

import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "candidateId", nullable = false)
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "recruitmentPositionId", nullable = false)
    private RecruitmentPosition recruitmentPosition;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String cvUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            columnDefinition = "ENUM('PENDING', 'HANDING', 'INTERVIEWING', 'WAITING_FOR_INTERVIEW_CONFIRM', 'REQUEST_RESCHEDULE', 'INTERVIEW_SCHEDULED', 'DESTROYED', 'REJECTED', 'DONE') DEFAULT 'PENDING'")
    private Progress progress;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime destroyAt;

    @Column(columnDefinition = "TEXT")
    private String destroyReason;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime interviewDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PASSED', 'FAILED') DEFAULT NULL")
    private ResultInterview resultInterview;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime confirmInterviewDate;

    @Column(columnDefinition = "TEXT")
    private String confirmInterviewDateReason;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime rejectedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectedReason;

    @Column
    private String interviewLink;
}