package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyRepo extends JpaRepository<BuyModel, Long> {


    Page<BuyModel> findAllByProductId(Long product_id, Pageable pageable);

    Page<BuyModel> findAllByProductCategoryUserId(Long product_category_user_id, Pageable pageable);


}
