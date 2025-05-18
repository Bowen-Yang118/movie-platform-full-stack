package parser;

public class Actor {
    private String id;
    private String name;
    private Integer birthYear;

    public Actor() {}

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public String getId() { return id; }
    public String getName() { return name; }
    public Integer getBirthYear() { return birthYear; }
}

