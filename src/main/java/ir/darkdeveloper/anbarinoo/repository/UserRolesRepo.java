package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserRoles;




@Repository
public interface UserRolesRepo extends JpaRepository<UserRoles, Long>{
    
    // @Query("SELECT role FROM UserRoles role WHERE role.name = :name")
    // public UserRoles findByName(String name);
    
    List<UserRoles> findByName(String name);

    @Query("SELECT model FROM UserRoles model WHERE model.name = :name")
    UserRoles getUSER(@Param("name") String name);
}
