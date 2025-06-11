package com.data.service.candidate;

import com.data.entity.Candidate;
import com.data.repository.candidate.CandidateRep;
import org.springframework.stereotype.Service;

@Service
public class CandidateServiceImp implements CandidateService {

    private CandidateRep candidateRep;
    public CandidateServiceImp(CandidateRep candidateRep) {
        this.candidateRep = candidateRep;
    }

    @Override
    public boolean register(Candidate candidate) {
        return candidateRep.register(candidate);
    }

    @Override
    public boolean checkPhoneNumber(String phoneNumber) {
        return candidateRep.checkPhoneNumber(phoneNumber);
    }
}
