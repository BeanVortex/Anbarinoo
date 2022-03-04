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
public class CategoryModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
//    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
//    @JsonIdentityReference(alwaysAsId = true)
    private UserModel user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private CategoryModel parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ToString.Exclude
    private List<CategoryModel> children = new LinkedList<>();

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

    public void addChild(CategoryModel children) {
        this.children.add(children);
    }

    public CategoryModel(Long id, String name, UserModel user, CategoryModel parent) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.parent = parent;
    }


}

