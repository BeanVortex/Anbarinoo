package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryModel, Long> {

    List<CategoryModel> findByNameContains(String name);

}
