package ir.darkdeveloper.anbarinoo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserModel;


@Repository
public interface UserRepo extends PagingAndSortingRepository<UserModel, Long> {

    @Query("SELECT model FROM UserModel model WHERE model.email = :username OR model.userName = :username")
    public UserModel findByEmailOrUsername(@Param("username") String username);

    public UserModel findUserById(Long id);

    public Page<UserModel> findAll(Pageable pageable);

    @Query("SELECT model.id FROM UserModel model WHERE model.email = :username OR model.userName = :username")
    public Long findUserIdByUsername(@Param("username") String username);
}
