package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.darkdeveloper.anbarinoo.model.RefreshModel;

@Repository
public interface RefreshRepo extends JpaRepository<RefreshModel, Long> {

    // for modifying should write these annotations
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshModel model WHERE model.userId=:id")
    void deleteTokenByUserId(@Param("id") Long id);

    @Query("SELECT model FROM RefreshModel model WHERE model.userId=:id")
    RefreshModel getRefreshByUserId(@Param("id") Long id);


    @Query("SELECT model.id FROM RefreshModel model WHERE model.userId=:id")
	Long getIdByUserId(@Param("id") Long adminId);


}
