package com.data.service.candidate;

import com.data.entity.Candidate;
import com.data.repository.candidate.CandidateRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateServiceImp implements CandidateService {

    private final CandidateRep candidateRep;

    @Override
    public Candidate register(Candidate candidate) {
        return candidateRep.register(candidate);
    }

    @Override
    public boolean checkPhoneNumber(String phoneNumber) {
        return candidateRep.checkPhoneNumber(phoneNumber);
    }

    @Override
    public Candidate getCandidateById(int id) {
        return candidateRep.getCandidateById(id);
    }

    @Override
    public List<Candidate> getFilteredCandidates(String search, String gender, Integer age, Integer experience, Integer technologyId, int page, int size) {
        return candidateRep.getFilteredCandidates(search, gender, age, experience, technologyId, page, size);
    }

    @Override
    public long countFilteredCandidates(String search, String gender, Integer age, Integer experience, Integer technologyId) {
        return candidateRep.countFilteredCandidates(search, gender, age, experience, technologyId);
    }


    @Override
    public boolean updateCandidate(Candidate candidate) {
        return candidateRep.updateCandidate(candidate);
    }
}
