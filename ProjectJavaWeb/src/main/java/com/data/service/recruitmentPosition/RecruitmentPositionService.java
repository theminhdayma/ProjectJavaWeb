package com.data.service.recruitmentPosition;

import com.data.entity.RecruitmentPosition;

import java.util.List;

public interface RecruitmentPositionService {
    List<RecruitmentPosition> findAll(String keyword, int page, int size);
    RecruitmentPosition findById(int id);
    boolean save(RecruitmentPosition recruitmentPosition);
    long countAll(String keyword);
    boolean update(RecruitmentPosition recruitmentPosition);
    boolean delete(int id);
    List<RecruitmentPosition> findAllByTechnology(String keyword,Integer technologyId, int page, int size);
    long countAllByTechnology(String keyword, Integer technologyId);
    List<RecruitmentPosition> findRandomPositionsExcluding(int excludeId, int limit);

}
