package ir.darkdeveloper.anbarinoo.repository.Financial;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;

@Repository
public interface ChequeRepo extends JpaRepository<ChequeModel, Long> {

    @EntityGraph(attributePaths = "user")
    List<ChequeModel> findChequeModelsByUser_Id(Long id);

    @EntityGraph(attributePaths = "user")
    List<ChequeModel> findChequeModelByPayToContains(String payTo);

}
