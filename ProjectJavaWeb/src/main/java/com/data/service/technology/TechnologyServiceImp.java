package com.data.service.technology;

import com.data.entity.Technology;
import com.data.repository.technology.TechnologyRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnologyServiceImp implements TechnologyService {

    private final TechnologyRep technologyRep;

    @Override
    public List<Technology> findAllTechnologies(int page, int size) {
        return technologyRep.findAllTechnologies(page, size);
    }

    @Override
    public List<Technology> findTechnologiesByName(String name, int page, int size) {
        return technologyRep.findTechnologiesByName(name, page, size);
    }

    @Override
    public long countAllTechnologies() {
        return technologyRep.countAllTechnologies();
    }

    @Override
    public long countTechnologiesByName(String name) {
        return technologyRep.countTechnologiesByName(name);
    }

    @Override
    public Technology findTechnologyById(int id) {
        return technologyRep.findTechnologyById(id);
    }

    @Override
    public boolean deleteTechnology(int id) {
        return technologyRep.deleteTechnology(id);
    }

    @Override
    public boolean saveTechnology(Technology technology) {
        return technologyRep.saveTechnology(technology);
    }

    @Override
    public boolean updateTechnology(Technology technology) {
        return technologyRep.updateTechnology(technology);
    }

    @Override
    public boolean checkNameTechnology(String name) {
        return technologyRep.checkNameTechnology(name);
    }

    @Override
    public List<Technology> findAllTechnologies() {
        return technologyRep.findAllTechnologies();
    }


}
