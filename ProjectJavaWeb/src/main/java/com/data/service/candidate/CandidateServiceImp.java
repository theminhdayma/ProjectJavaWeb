package com.data.service.candidate;

import com.data.entity.Candidate;
import com.data.repository.candidate.CandidateRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
