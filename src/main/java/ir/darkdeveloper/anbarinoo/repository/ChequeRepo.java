package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;

@Repository
public interface ChequeRepo extends JpaRepository<ChequeModel, Long> {


    List<ChequeModel> findChequeModelsByUser_Id(Long id);

    List<ChequeModel> findChequeModelByPayToContains(String payTo);

//    @Query("SELECT model FROM ChequeModel model WHERE model.user.id = :id")
//    List<ChequeModel> findChequeModelByUserId(@Param("id") Long id);
}
