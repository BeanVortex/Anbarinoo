package ir.darkdeveloper.anbarinoo.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserRole;


@Repository
public interface UserRolesRepo extends JpaRepository<UserRole, Long> {

    Set<UserRole> findAllByName(String name);

    Optional<UserRole> findByName(String name);
}
