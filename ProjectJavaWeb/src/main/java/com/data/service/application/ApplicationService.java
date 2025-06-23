package com.data.service.application;

import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;

import java.util.List;

public interface ApplicationService {
    List<Application> findAll(String keyword, Progress progress, ResultInterview resultInterview, int page, int size);
    List<Application> findAllByCandidateId(int candidateId, int page, int size);
    Application findById(int id);
    long countAll(String keyword, Progress progress, ResultInterview resultInterview);
    Application save(Application application);
    long countByCandidateId(int candidateId);
    boolean update(Application application);
}
