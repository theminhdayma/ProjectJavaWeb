package com.data.service.candidate;

import com.data.entity.Candidate;
import java.util.List;

public interface CandidateService {
    // Các phương thức hiện tại
    Candidate register(Candidate candidate);
    boolean checkPhoneNumber(String phoneNumber);
    Candidate getCandidateById(int id);

    // Phương thức lọc tổng hợp duy nhất (không có technology)
    List<Candidate> getFilteredCandidates(String search, String gender, Integer age,
                                          Integer experience, int page, int size);
    long countFilteredCandidates(String search, String gender, Integer age,
                                 Integer experience);
    boolean updateCandidate(Candidate candidate);
}
