import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserCSV {
    public static Map<String, Regions> readFile(String path) throws IOException{
        Map<String, Regions> regions = new HashMap<>();
            List<String> lines = Files.readAllLines(Paths.get(path), Charset.forName("windows-1251"));
            lines = lines.subList(1, lines.size());

            for (String line : lines) {
                String[] data = line.split(",", 2);
                data = data[1].toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String region = data[1].replaceAll("г. ", "");
                if (data.length == 4  && !data[0].isEmpty() && !data[1].isEmpty() && !data[2].isEmpty() && !data[3].isEmpty()) {
                    Regions regionObject = regions.computeIfAbsent(region, Regions::new);
                    regionObject.addSportObject(new SportObjects(data[0], data[2], data[3]));
                }
            }
        return regions;
    }
}