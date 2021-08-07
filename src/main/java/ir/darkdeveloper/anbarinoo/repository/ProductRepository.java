package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ProductModel;

import java.util.Optional;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductModel, Long> {

    Page<ProductModel> findByNameContains(String name, Pageable pageable);

    Page<ProductModel> findAllByUserId(Long user_id, Pageable pageable);

}
