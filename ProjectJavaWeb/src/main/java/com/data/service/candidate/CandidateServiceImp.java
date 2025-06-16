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
    public boolean register(Candidate candidate) {
        return candidateRep.register(candidate);
    }

    @Override
    public boolean checkPhoneNumber(String phoneNumber) {
        return candidateRep.checkPhoneNumber(phoneNumber);
    }

    @Override
    public List<Candidate> getCandidates(int page, int size) {
        return candidateRep.getCandidates(page, size);
    }

    @Override
    public List<Candidate> getCandidatesByName(String name, int page, int size) {
        return candidateRep.getCandidatesByName(name, page, size);
    }

    @Override
    public long countAllCandidates() {
        return candidateRep.countAllCandidates();
    }

    @Override
    public long countCandidatesByName(String name) {
        return candidateRep.countCandidatesByName(name);
    }

    @Override
    public Candidate getCandidateById(int id) {
        return candidateRep.getCandidateById(id);
    }
}
