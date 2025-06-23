package com.data.repository.application;

import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;

import java.util.List;

public interface ApplicationRep {
    List<Application> findAll(String keyword, Progress progress, ResultInterview resultInterview, int page, int size);
    List<Application> findAllByCandidateId(int candidateId, int page, int size);
    Application findById(int id);
    Application save(Application application);
    long countAll(String keyword, Progress progress, ResultInterview resultInterview);
    long countByCandidateId(int candidateId);
    boolean update(Application application);
}
