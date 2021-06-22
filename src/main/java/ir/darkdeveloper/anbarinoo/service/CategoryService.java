package ir.darkdeveloper.anbarinoo.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.repository.CategoryRepo;

@Service
public class CategoryService {

    private final CategoryRepo repo;

    @Autowired
    public CategoryService(CategoryRepo repo) {
        this.repo = repo;
    }


    @Transactional
    public CategoryModel saveCategory(CategoryModel model) {
        return repo.save(model);
    }

    public List<CategoryModel> findByNameContains(String name) {
        return repo.findByNameContains(name);
    }
    
}
