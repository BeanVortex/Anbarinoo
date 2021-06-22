package ir.darkdeveloper.anbarinoo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;

@SpringBootTest()
public class CategoryServiceTest {


    @Autowired
    private CategoryService service;


    @Test
    void save() {
        CategoryModel cm = new CategoryModel();
        cm.setName("n1");
        service.saveCategory(cm);
    }
}
