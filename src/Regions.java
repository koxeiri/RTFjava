import java.util.ArrayList;
import java.util.List;


public class Regions {
    private final String region;
    private final List<SportObjects> sportObjectsList = new ArrayList<>();

    public Regions(String region){
        this.region = region;
    }

    public void addSportObject(SportObjects sportObject) {
        sportObjectsList.add(sportObject);
    }

    public SportObjects[] getObjects() {
        SportObjects[] objectsArray = new SportObjects[sportObjectsList.size()];
        sportObjectsList.toArray(objectsArray);
        return objectsArray;
    }

    public String getRegion() {
        return region;
    }
}
