package ir.darkdeveloper.anbarinoo.repository.Financial;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.SellModel;

@Repository
public interface SellRepo extends PagingAndSortingRepository<SellModel, Long> {

    @EntityGraph(attributePaths = "product.category.user")
    Page<SellModel> findAllByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "product.category.user")
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
}
