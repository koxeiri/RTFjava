// Класс, представляющий данные о спортивном объекте
public class SportObjects {
    private final String name;
    private final String address;
    private final String date;

    public SportObjects(String name, String address, String date) {
        this.name = name;
        this.address = address;
        this.date = date;
    }


    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }
}