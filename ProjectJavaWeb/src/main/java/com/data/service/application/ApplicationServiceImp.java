package com.data.service.application;

import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;
import com.data.repository.application.ApplicationRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImp implements ApplicationService {

    private final ApplicationRep applicationRep;


    @Override
    public List<Application> findAll(String keyword, Progress progress, ResultInterview resultInterview, int page, int size) {
        return applicationRep.findAll(keyword, progress, resultInterview, page, size);
    }

    @Override
    public List<Application> findAllByCandidateId(int candidateId, int page, int size) {
        return applicationRep.findAllByCandidateId(candidateId, page, size);
    }

    @Override
    public Application findById(int id) {
        return applicationRep.findById(id);
    }

    @Override
    public long countAll(String keyword, Progress progress, ResultInterview resultInterview) {
        return applicationRep.countAll(keyword, progress, resultInterview);
    }

    @Override
    public Application save(Application application) {
        return applicationRep.save(application);
    }

    @Override
    public long countByCandidateId(int candidateId) {
        return applicationRep.countByCandidateId(candidateId);
    }

    @Override
    public boolean update(Application application) {
        return applicationRep.update(application);
    }
}
