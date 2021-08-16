package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellsRepo extends PagingAndSortingRepository<SellsModel, Long> {

    Page<SellsModel> findAllByProductId(Long product_id, Pageable pageable);

    Page<SellsModel> findAllByProductUserId(Long product_user_id, Pageable pageable);

}
