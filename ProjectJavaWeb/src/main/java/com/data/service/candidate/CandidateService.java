package com.data.service.candidate;

import com.data.entity.Candidate;

public interface CandidateService {
    boolean register(Candidate candidate);
    boolean checkPhoneNumber(String phoneNumber);
}
