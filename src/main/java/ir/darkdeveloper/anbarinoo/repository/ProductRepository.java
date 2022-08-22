package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ProductModel;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductModel, Long> {

    @Query("select m from ProductModel m " +
            "where upper(m.name) like upper(concat('%',:name,'%')) " +
            "and m.category.user.id = :userId")
    Page<ProductModel> findByNameContainsAndUserId(String name, Long userId, Pageable pageable);

    @Query("select m from ProductModel m " +
            "where m.category.user.id = :userId")
    Page<ProductModel> findAllByUserId(Long userId, Pageable pageable);

    @Query("select m from ProductModel m " +
            "where m.category.user.id = :userId")
    List<ProductModel> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"category.user", "images"})
    Optional<ProductModel> findById(Long id);

}
