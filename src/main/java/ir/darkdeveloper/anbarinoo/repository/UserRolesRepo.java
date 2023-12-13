package ir.darkdeveloper.anbarinoo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ir.darkdeveloper.anbarinoo.model.UserRole;


@Repository
public interface UserRolesRepo extends JpaRepository<UserRole, Long> {

    @EntityGraph(attributePaths = "authorities")
    Set<UserRole> findAllByName(String name);

    @EntityGraph(attributePaths = "authorities")
    List<UserRole> findAll();

    @EntityGraph(attributePaths = "authorities")
    Optional<UserRole> findByName(String name);
}
