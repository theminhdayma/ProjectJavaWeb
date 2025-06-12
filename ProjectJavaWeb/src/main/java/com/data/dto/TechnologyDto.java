package com.data.dto;

import com.data.entity.Candidate;
import com.data.entity.RecruitmentPosition;
import com.data.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnologyDto {
    private int id;

    @NotBlank(message = "Tên công nghệ không được để trống")
    private String name;

    private Status status;

    private List<Candidate> candidates;

    private List<RecruitmentPosition> positions;
}
