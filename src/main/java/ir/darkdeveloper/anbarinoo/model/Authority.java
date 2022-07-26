package ir.darkdeveloper.anbarinoo.model;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    
    OP_ACCESS_ADMIN,
    OP_EDIT_ADMIN,
    OP_ADD_ADMIN,
    OP_DELETE_ADMIN,

    OP_ADD_ROLE,
    OP_ACCESS_ROLE,
    OP_DELETE_ROLE,

    OP_ADD_USER,
    OP_ACCESS_USER,
    OP_EDIT_USER,
    OP_DELETE_USER,

    OP_ADD_PRODUCT,
    OP_EDIT_PRODUCT,
    OP_DELETE_PRODUCT;

    @Override
    public String getAuthority() {
        return this.name();
    }

}
