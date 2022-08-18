package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.BuyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BuyRepo extends JpaRepository<BuyModel, Long> {


    Page<BuyModel> findAllByProductId(Long userId, Pageable pageable);

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


}
