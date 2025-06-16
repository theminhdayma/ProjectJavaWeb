package com.data.service.candidate;

import com.data.entity.Candidate;

import java.util.List;

public interface CandidateService {
    boolean register(Candidate candidate);
    boolean checkPhoneNumber(String phoneNumber);
    List<Candidate> getCandidates(int page, int size);
    List<Candidate> getCandidatesByName(String name, int page, int size);
    long countAllCandidates();
    long countCandidatesByName(String name);
    Candidate getCandidateById(int id);
}
