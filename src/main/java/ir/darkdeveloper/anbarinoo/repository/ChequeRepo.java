package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;

import java.util.List;

@Repository
public interface ChequeRepo extends JpaRepository<ChequeModel, Long> {


    List<ChequeModel> findChequeModelsByUser_Id(Long id);

}
