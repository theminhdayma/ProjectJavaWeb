package com.data.repository.candidate;

import com.data.entity.Candidate;

public interface CandidateRep {
    boolean register(Candidate candidate);
    boolean checkPhoneNumber(String phoneNumber);
}
