package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ProductModel;


@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductModel, Long> {

    Page<ProductModel> findByNameContainsAndCategoryUserId(String name, Long category_user_id, Pageable pageable);

    Page<ProductModel> findAllByCategoryUserId(Long category_user_id, Pageable pageable);

}
