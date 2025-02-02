import lombok.Getter;

public class Person {

    @Getter public String name;
    @Getter public int age;

    public Person(){}

    public Person(String name, int age){
        this.age = age;
        this.name = name;
    }

}
