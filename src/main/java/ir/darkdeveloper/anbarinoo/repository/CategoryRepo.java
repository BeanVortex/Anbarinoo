package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;

import jakarta.persistence.Entity;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryModel, Long> {

    // Also, ENTITY GRAPH is a solution
    @EntityGraph(attributePaths = {"user.roles.authorities"})
    @Query("""
             SELECT model FROM CategoryModel model
             WHERE model.user.id = :userId
            """)
    List<CategoryModel> findAllByUserId(@Param("userId") Long user_id);


    @EntityGraph(attributePaths = {"user.roles.authorities"})
    Optional<CategoryModel> findById(Long aLong);
}
