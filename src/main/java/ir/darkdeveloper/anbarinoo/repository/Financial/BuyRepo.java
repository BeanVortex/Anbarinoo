package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BuyRepo extends JpaRepository<BuyModel, Long> {


    @EntityGraph(attributePaths = "product")
    Page<BuyModel> findAllByProductId(Long productId, Pageable pageable);

    @Query("SELECT b FROM BuyModel b LEFT JOIN FETCH ProductModel p on b.product = p AND p.category.user.id = :userId")
    Page<BuyModel> findAllByProductCategoryUserId(Long userId, Pageable pageable);

    Page<BuyModel> findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<BuyModel> findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(
            Long productId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    @EntityGraph(attributePaths = "product.category.user")
    Optional<BuyModel> findById(Long aLong);

    @Transactional
    @Modifying
    @Query("UPDATE BuyModel b set b.product = null where b.product = :product")
    void updateNullProduct(ProductModel product);

}
