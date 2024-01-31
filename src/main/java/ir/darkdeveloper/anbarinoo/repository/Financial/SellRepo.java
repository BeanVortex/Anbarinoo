package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.SellModel;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SellRepo extends PagingAndSortingRepository<SellModel, Long>, ListCrudRepository<SellModel, Long> {

    @EntityGraph(attributePaths = "product")
    Page<SellModel> findAllByProductId(Long productId, Pageable pageable);

    @Query("SELECT b FROM SellModel b LEFT JOIN FETCH ProductModel p on b.product = p AND p.category.user.id = :userId")
    Page<SellModel> findAllByProductCategoryUserId(Long userId, Pageable pageable);


    Page<SellModel> findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<SellModel> findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(
            Long productId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    @EntityGraph(attributePaths = "product.category.user")
    Optional<SellModel> findById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE SellModel b set b.product = null where b.product = :product")
    void updateNullProduct(ProductModel product);

}
