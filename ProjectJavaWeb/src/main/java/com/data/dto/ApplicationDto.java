package com.data.dto;

import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDto {
    private int id;

    @NotNull(message = "Candidate ID không được để trống")
    private int candidateId;

    @NotNull(message = "Recruitment Position ID không được để trống")
    private int recruitmentPositionId;

    @NotBlank(message = "CV URL không được để trống")
    private String cvUrl;

    @NotNull(message = "Progress không được để trống")
    private Progress progress;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime destroyAt;
    private String destroyReason;
    private LocalDateTime interviewDate;
    private ResultInterview resultInterview;
    private LocalDateTime confirmInterviewDate;
    private String confirmInterviewDateReason;
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime rejectedAt;
    private String rejectedReason;

    private String candidateName;
    private String candidateEmail;
    private String positionTitle;
    private String departmentName;

    private String createdAtFormatted;
    private String interviewDateFormatted;
    private String confirmInterviewDateFormatted;
    private String rejectedAtFormatted;
    private String destroyAtFormatted;
}
