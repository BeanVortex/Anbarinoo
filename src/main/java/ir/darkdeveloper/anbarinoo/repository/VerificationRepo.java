package ir.darkdeveloper.anbarinoo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.VerificationModel;

@Repository
public interface VerificationRepo extends JpaRepository<VerificationModel, Long> {

    @Query("SELECT model FROM VerificationModel model WHERE model.token = :token")
    Optional<VerificationModel> findByToken(@Param("token") String token);
}
