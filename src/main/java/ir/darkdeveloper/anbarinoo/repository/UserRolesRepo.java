package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserRoles;


@Repository
public interface UserRolesRepo extends JpaRepository<UserRoles, Long> {

    Set<UserRoles> findAllByName(String name);

    Optional<UserRoles> findByName(String name);
}
