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
    public List<RecruitmentPosition> findAll(int page, int size) {
        return recruitmentPositionRep.findAll(page, size);
    }

    @Override
    public RecruitmentPosition findById(int id) {
        return recruitmentPositionRep.findById(id);
    }
}
