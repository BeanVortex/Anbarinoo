package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.repository.UserRolesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserRolesService {

    private final UserRolesRepo repo;

    @Transactional
    public String saveRole(UserRole role) {
        repo.save(role);
        return "Role created";
    }

    public List<UserRole> getAllRoles() {
        return repo.findAll();
    }

    public Set<UserRole> findAllByName(String name) {
        return repo.findAllByName(name);
    }

    public String deleteRole(Long id) {
        repo.deleteById(id);
        return "Role deleted";
    }

    public Boolean exists(String name) {
        return repo.findByName(name).isPresent();
    }


}
