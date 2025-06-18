package com.data.repository.recruitmentPosition;

import com.data.entity.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionRep {
    List<RecruitmentPosition> findAll(int page, int size);
    RecruitmentPosition findById(int id);
}
