package ir.darkdeveloper.anbarinoo.model;


import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CategoryModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserModel user;


    @ManyToOne
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private CategoryModel parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ToString.Exclude
    @Builder.Default
    private List<CategoryModel> children = new LinkedList<>();

    // bidirectional: it means that the foreign key is on the other side
    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ToString.Exclude
    private List<ProductModel> products;

    public CategoryModel(String name) {
        this.name = name;
    }

    public CategoryModel(Long id) {
        this.id = id;
    }

    public CategoryModel(String name, CategoryModel parent) {
        this.name = name;
        this.parent = parent;
    }

    public CategoryModel(Long id, String name, UserModel user, CategoryModel parent) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.parent = parent;
    }


}

