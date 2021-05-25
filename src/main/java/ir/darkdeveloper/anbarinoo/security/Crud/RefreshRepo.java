package ir.darkdeveloper.anbarinoo.security.Crud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepo extends JpaRepository<RefreshModel, Long> {

    @Query("DELETE FROM RefreshModel model WHERE model.userId=:id")
    void deleteTokenByUserId(@Param("id") Long id);

    @Query("SELECT model FROM RefreshModel model WHERE model.userId=:id")
    RefreshModel getRefreshByUserId(@Param("id") Long id);

    @Query("UPDATE RefreshModel model SET model.accessToken = :token WHERE model.userId=:id ")
    RefreshModel updateTokenByUserId(@Param("id") Long userId, @Param("token") String accessToken);

    @Query("SELECT model.id FROM RefreshModel model WHERE model.userId=:id")
	Long getIdByUserId(@Param("id") Long adminId);


}
