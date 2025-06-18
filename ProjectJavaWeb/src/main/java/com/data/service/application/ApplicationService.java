package com.data.service.application;

import com.data.entity.Application;

import java.util.List;

public interface ApplicationService {
    List<Application> findAll(int page, int size);
    List<Application> findAllByCandidateId(int candidateId, int page, int size);
    Application findById(int id);
    boolean save(Application application);
}
