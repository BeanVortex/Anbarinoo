package ir.darkdeveloper.anbarinoo.repository.Financial;

import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DebtOrDemandRepo extends JpaRepository<DebtOrDemandModel, Long> {

    Page<DebtOrDemandModel> findAllByUserId(Long userId, Pageable pageable);

    Optional<DebtOrDemandModel> findByChequeId(Long chequeId);

    Page<DebtOrDemandModel> findAllByUserIdAndIsDebtAndIsCheckedOutAndCreatedAtAfterAndCreatedAtBefore(
            Long userId,
            Boolean isDebt,
            Boolean isCheckedOut,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);
}
