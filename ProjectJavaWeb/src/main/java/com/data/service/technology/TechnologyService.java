package com.data.service.technology;

import com.data.entity.Technology;

import java.util.List;

public interface TechnologyService {
    List<Technology> findAllTechnologies(int page, int size);
    List<Technology> findTechnologiesByName(String name, int page, int size);
    long countAllTechnologies();
    long countTechnologiesByName(String name);
    Technology findTechnologyById(int id);
    boolean deleteTechnology(int id);
    boolean saveTechnology(Technology technology);
    boolean updateTechnology(Technology technology);
    boolean checkNameTechnology(String name);
    List<Technology> findAllTechnologies();
}
