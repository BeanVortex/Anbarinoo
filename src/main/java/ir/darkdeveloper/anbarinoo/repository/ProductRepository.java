package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ProductModel;

import java.util.List;


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

}
