import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;

public class Main {
    private static DatabaseConnect dataBase;

    public static void main(String[] args) throws ClassNotFoundException , SQLException , IOException{
            // Импорт данных из CSV и вставка в базу данных
            importDataFromCSV("Объекты спорта.csv");

            // Вывод всех данных
            //displayDatabaseData();

            // Задание 1: Построить гистрограмму объектов по регионам
            System.out.println("Задание №1");
            buildGist();
            System.out.println();

            // Задание 2: Вывести среднее количество объектов спорта в регионах в консоль
            System.out.println("Задание №2");
            displayCountAvg();
            System.out.println();

            // Задание 3: Вывести топ-3 региона по спортивным объектам
            System.out.println("Задание №3");
            displayCountObj();
            dataBase.close();
    }

    //Для импорта и чтения файла
    private static void importDataFromCSV(String csvFilePath) throws ClassNotFoundException, SQLException, IOException {
        var regions = ParserCSV.readFile(csvFilePath);
        dataBase = new DatabaseConnect();

        regions.values().forEach(region -> {
            try {
                dataBase.insertRegion(region);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // Для вывода построчно всех объектов спорта
    private static void displayDatabaseData() throws SQLException {
        System.out.println("Спортивные объекты РФ:");
        System.out.println(String.join("\n", dataBase.getAllData()));
        System.out.println();
    }

    // Для построения гистрограммы
    private static void buildGist() throws SQLException, IOException {
        JFreeChart chart = ChartFactory.createBarChart("Количество объектов спорта по регионам", "Регион", "Количество объектов", dataBase.getGistObj());
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
        ChartUtils.saveChartAsPNG(new File("graphic.png"), chart, 1920, 1080);
        System.out.println("Гистрограмма построена !");
    }

    // Для вывода среднего кол-ва объектов в регионах
    private static void displayCountAvg() throws SQLException {
        System.out.println("Среднее кол-во объектов спорта в регионах: " + dataBase.getCountAvg());
    }

    // Для вывода топ-3 регионов по кол-ву спортивных объектов
    private static void displayCountObj() throws SQLException {
        System.out.println("Топ-3 региона с наибольшим количеством спортивных объектов: ");
        System.out.println(String.join(", ", dataBase.getCountObj()));
    }
}
