package ir.darkdeveloper.anbarinoo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.darkdeveloper.anbarinoo.model.UserModel;

@Repository
public interface UserRepo extends PagingAndSortingRepository<UserModel, Long> {

    @Query("select model from UserModel model where model.email = :username or model.userName = :username")
    Optional<UserModel> findByEmailOrUsername(@Param("username") String username);

    @Query("select model from UserModel model")
    Page<UserModel> getAll(Pageable pageable);


    @Transactional
    @Modifying
    @Query("update UserModel m set m.enabled = :value where m.id = :id")
    void updateEnabledById(Long id, Boolean value);

    @Query("select new UserModel (model.id, model.email, model.userName, model.enabled, model.shopImage," +
            " model.profileImage, model.shopName, model.createdAt, model.updatedAt, model.address," +
            " model.description, model.provider)" +
            " from UserModel as model where model.id = :id")
    Optional<UserModel> getSimpleUserInfo(@Param("id") Long id);


    @EntityGraph(attributePaths = {"roles", "roles.authorities"})
    Optional<UserModel> findById(Long aLong);
}
