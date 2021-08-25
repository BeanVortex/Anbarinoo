package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtOrDemandRepo extends JpaRepository<DebtOrDemandModel, Long> {

    Page<DebtOrDemandModel> findAllByUserId(Long userId, Pageable pageable);
}
