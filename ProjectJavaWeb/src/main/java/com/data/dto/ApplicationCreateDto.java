package com.data.dto;

import com.data.entity.enums.Progress;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationCreateDto {
    @NotNull(message = "Candidate ID không được để trống")
    private int candidateId;

    @NotNull(message = "Recruitment Position ID không được để trống")
    private int recruitmentPositionId;

    @NotNull(message = "CV file không được để trống")
    private MultipartFile cvFile;

    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull(message = "Progress không được để trống")
    private Progress progress;
}
