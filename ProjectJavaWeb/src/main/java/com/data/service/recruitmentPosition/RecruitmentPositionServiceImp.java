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
    public long countAll() {
        return recruitmentPositionRep.countAll();
    }

    @Override
    public boolean update(RecruitmentPosition recruitmentPosition) {
        return recruitmentPositionRep.update(recruitmentPosition);
    }

    @Override
    public boolean delete(int id) {
        return recruitmentPositionRep.delete(id);
    }
}
