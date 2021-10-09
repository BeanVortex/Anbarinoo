package ir.darkdeveloper.anbarinoo.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.repository.UserRolesRepo;

@Service
public class UserRolesService {

    private final UserRolesRepo repo;

    @Autowired
    public UserRolesService(UserRolesRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public ResponseEntity<?> saveRole(UserRole role) {
        try {
            repo.save(role);
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<UserRole> getAllRoles() {
        return repo.findAll();
    }

    public Set<UserRole> findAllByName(String name) {
        return repo.findAllByName(name);
    }

    public ResponseEntity<?> deleteRole(Long id) {
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Boolean exists(String name) {
        return repo.findByName(name).isPresent();
    }

}
