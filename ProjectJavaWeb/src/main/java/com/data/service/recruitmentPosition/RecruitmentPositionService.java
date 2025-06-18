package com.data.service.recruitmentPosition;

import com.data.entity.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionService {
    List<RecruitmentPosition> findAll(int page, int size);
    RecruitmentPosition findById(int id);
}
