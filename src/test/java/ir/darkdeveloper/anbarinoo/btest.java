package ir.darkdeveloper.anbarinoo;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
import org.junit.runners.MethodSorters;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class btest {

    private static StringBuilder output = new StringBuilder();

    @Test
    @Order(1)
    public void t2() {
        output.append("2");
    }

    @Test
    @Order(2)
    public void t1() {
        output.append("1");
    }

    @Test
    @Order(3)
    public void t3() {
        output.append("3");
    }

    @AfterAll
    public static void assertOutput() {
        assertThat(output.toString()).isEqualTo("213");
    }
}
