package po.vysniakov.currencie;

public class Currency {
    private Long id;
    private String name;
    private String code;
    private String sing;

    public Currency() {
    }

    public Currency(Long id, String name, String code, String sing) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.sing = sing;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSing() {
        return sing;
    }

    public void setSing(String sing) {
        this.sing = sing;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", sing='" + sing + '\'' +
                '}';
    }
}
