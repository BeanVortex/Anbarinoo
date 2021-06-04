package ir.darkdeveloper.anbarinoo.model;

import java.security.AuthProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.util.ImageUtil;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserModel implements UserDetails, ImageUtil, OAuth2User {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, unique = true)
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    // TODO: Enable for oauth logs and disable for local logs
    private Boolean enabled = true;

    private String providerId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile file;

    @Column(name = "profile")
    private String profilePicture;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role", referencedColumnName = "name"))
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<UserRoles> roles;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String shopName;

    private String shopImage;

    private String address;

    private String description;

    //mappedBy is read-only. can't add product while creating user
    @OneToMany(mappedBy = "user")
    private List<ProductModel> products;

    // For saving products I need it
    public UserModel(Long id) {
        this.id = id;
    }

    public UserModel() {
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auth = new ArrayList<>();
        if (roles != null)
            roles.forEach(e -> auth.addAll(e.getAuthorities()));
        else
            auth.add(Authority.OP_ACCESS_USER);
        return auth;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isEnabled() {
        return this.getEnabled();
    }

    @Override
    public String getImage() {
        return profilePicture;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return new HashMap<>();
    }

    @Override
    public String getName() {
        return userName;
    }

}
