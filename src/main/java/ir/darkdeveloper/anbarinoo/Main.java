package ir.darkdeveloper.anbarinoo;

public class Main {
    public static void main(String[] args) {
        Car c = new Car();
        System.out.println(c.name());
        System.out.println(c.brand());
    }
}


record Car(
        String name,
        String brand
) {
    public Car {
        System.out.println("cons");
    }

    public Car() {
        this(null, null);
        System.out.println("emty cons");
    }

    public Car(String name) {
        this(name, null);
        System.out.println("one arg cons");
    }

}