package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.SellModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SellRepo extends PagingAndSortingRepository<SellModel, Long>, ListCrudRepository<SellModel, Long> {

    @EntityGraph(attributePaths = "product")
    Page<SellModel> findAllByProductId(Long productId, Pageable pageable);

    Page<SellModel> findAllByUserId(Long userId, Pageable pageable);


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

    Optional<SellModel> findById(Long id);

}
