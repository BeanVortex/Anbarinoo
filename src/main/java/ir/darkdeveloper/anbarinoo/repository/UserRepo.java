package ir.darkdeveloper.anbarinoo.repository;

import io.micrometer.core.lang.NonNullApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface UserRepo extends PagingAndSortingRepository<UserModel, Long> {

    @Query("SELECT model FROM UserModel model WHERE model.email = :username OR model.userName = :username")
    UserModel findByEmailOrUsername(@Param("username") String username);

    Optional<UserModel> findUserById(Long id);


    @NonNull
    Page<UserModel> findAll(@NonNull Pageable pageable);

    @Query("SELECT model.id FROM UserModel model WHERE model.email = :username OR model.userName = :username")
    Long findUserIdByUsername(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE UserModel m SET m.enabled = true WHERE m.id = :id")
    void trueEnabledById(@Param("id") Long id);

    @Query("SELECT new UserModel (model.id, model.email, model.userName, model.enabled, model.shopImage," +
            " model.profileImage, model.shopName, model.createdAt, model.updatedAt, model.address," +
            " model.description, model.provider)" +
            " FROM UserModel as model WHERE model.id = :id")
    Optional<UserModel> getSimpleUserInfo(@Param("id") Long id);

}
