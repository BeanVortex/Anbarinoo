package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellRepo extends PagingAndSortingRepository<SellModel, Long> {

    Page<SellModel> findAllByProductId(Long product_id, Pageable pageable);

    Page<SellModel> findAllByProductCategoryUserId(Long product_category_user_id, Pageable pageable);

}
