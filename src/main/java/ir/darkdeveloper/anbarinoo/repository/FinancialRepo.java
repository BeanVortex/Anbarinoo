package ir.darkdeveloper.anbarinoo.repository;

import ir.darkdeveloper.anbarinoo.model.FinancialModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialRepo extends JpaRepository<FinancialModel, Long> {

}
