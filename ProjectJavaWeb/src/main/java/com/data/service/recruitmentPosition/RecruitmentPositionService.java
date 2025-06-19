package com.data.service.recruitmentPosition;

import com.data.entity.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionService {
    List<RecruitmentPosition> findAll(String keyword, int page, int size);
    RecruitmentPosition findById(int id);
    boolean save(RecruitmentPosition recruitmentPosition);
    long countAll();
    boolean update(RecruitmentPosition recruitmentPosition);
    boolean delete(int id);
}
