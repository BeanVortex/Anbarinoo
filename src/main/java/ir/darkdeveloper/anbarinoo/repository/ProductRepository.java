package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ProductModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductModel, Long>, ListCrudRepository<ProductModel, Long> {

    @Query("select m from ProductModel m " +
            "where upper(m.name) like upper(concat('%',:name,'%')) " +
            "and m.user.id = :userId")
    Page<ProductModel> findByNameContainsAndUserId(String name, Long userId, Pageable pageable);

    Page<ProductModel> findAllByUserId(Long userId, Pageable pageable);

    List<ProductModel> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "images"})
    Optional<ProductModel> findById(Long id);

    @Modifying
    @Query("UPDATE ProductModel p set p.totalCount = ?1 where p.id = ?2")
    void totalCount(BigDecimal totalCount, Long productId);
}
