package com.data.service.recruitmentPosition;

import com.data.entity.RecruitmentPosition;
import com.data.repository.recruitmentPosition.RecruitmentPositionRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentPositionServiceImp implements RecruitmentPositionService {

    private final RecruitmentPositionRep recruitmentPositionRep;

    @Override
    public List<RecruitmentPosition> findAll(String keyword, int page, int size) {
        return recruitmentPositionRep.findAll(keyword, page, size);
    }

    @Override
    public RecruitmentPosition findById(int id) {
        return recruitmentPositionRep.findById(id);
    }

    @Override
    public boolean save(RecruitmentPosition recruitmentPosition) {
        return recruitmentPositionRep.save(recruitmentPosition);
    }

    @Override
    public long countAll(String keyword) {
        return recruitmentPositionRep.countAll(keyword);
    }

    @Override
    public boolean update(RecruitmentPosition recruitmentPosition) {
        return recruitmentPositionRep.update(recruitmentPosition);
    }

    @Override
    public boolean delete(int id) {
        return recruitmentPositionRep.delete(id);
    }

    @Override
    public List<RecruitmentPosition> findAllByTechnology(String keyword, Integer technologyId, int page, int size) {
        return recruitmentPositionRep.findAllByTechnology(keyword, technologyId, page, size);
    }

    @Override
    public long countAllByTechnology(String keyword, Integer technologyId) {
        return recruitmentPositionRep.countAllByTechnology(keyword, technologyId);
    }

    @Override
    public List<RecruitmentPosition> findRandomPositionsExcluding(int excludeId, int limit) {
        return recruitmentPositionRep.findRandomPositionsExcluding(excludeId, limit);
    }
}
