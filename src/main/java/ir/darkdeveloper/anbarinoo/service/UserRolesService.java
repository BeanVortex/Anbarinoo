package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.NotFoundException;
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
    public UserRole saveRole(UserRole role) {
        return repo.save(role);
    }

    public List<UserRole> getAllRoles() {
        return repo.findAll();
    }

    public Set<UserRole> findAllByName(String name) {
        return repo.findAllByName(name);
    }

    public String deleteRole(Long id) {
        repo.deleteById(id);
        return "Role deleted with id: " + id;
    }

    public Boolean exists(String name) {
        return repo.findByName(name).isPresent();
    }


    public UserRole getRole(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Role with id=" + id + " not found"));
    }
}
