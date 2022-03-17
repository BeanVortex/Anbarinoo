package ir.darkdeveloper.anbarinoo.repository.Financial;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.SellModel;

@Repository
public interface SellRepo extends PagingAndSortingRepository<SellModel, Long> {

    Page<SellModel> findAllByProductId(Long productId, Pageable pageable);

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


}
