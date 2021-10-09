package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
