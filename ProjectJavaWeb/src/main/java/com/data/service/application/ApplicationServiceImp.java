package com.data.service.application;

import com.data.entity.Application;
import com.data.repository.application.ApplicationRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImp implements ApplicationService {

    private final ApplicationRep applicationRep;

    @Override
    public List<Application> findAll(int page, int size) {
        return applicationRep.findAll(page, size);
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
    public boolean save(Application application) {
        return applicationRep.save(application);
    }

    @Override
    public long countAll() {
        return applicationRep.countAll();
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
