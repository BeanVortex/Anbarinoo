package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.dto.UserRoleDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.UserRoleMapper;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/role")
@RequiredArgsConstructor
public class UserRolesController {

    private final UserRolesService service;
    private final UserRoleMapper mapper;


    @PostMapping("/")
    @PreAuthorize("hasAuthority('OP_ADD_ROLE')")
    public ResponseEntity<UserRoleDto> saveRole(@RequestBody UserRole role) {
        return new ResponseEntity<>(mapper.userRoleToDto(service.saveRole(role)), HttpStatus.CREATED);
    }

    record UserRoleDtos(List<UserRoleDto> userRoles) {
    }

    @GetMapping("/all/")
    @PreAuthorize("hasAuthority('OP_ACCESS_ROLE')")
    public ResponseEntity<UserRoleDtos> getAllRoles() {
        return ResponseEntity.ok(new UserRoleDtos(service.getAllRoles().stream().map(mapper::userRoleToDto).toList()));

    }
    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_ROLE')")
    public ResponseEntity<UserRoleDto> getRole(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.userRoleToDto(service.getRole(id)));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_ROLE')")
    public ResponseEntity<String> deleteRole(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.deleteRole(id));
    }
}
