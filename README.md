# Итоговый проект по Java 3 вариант
1. Создадим классы Regions.java и SportObjects.java<br>
Regions.java - содержит данные о регионе и его спортивных объектах<br>
SportObjects.java - содержит данные непосредственно о спортивном объекте<br>
<b>Regions.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/ef1a4d27-78b5-453a-9171-94ec84a87ebd)

<br>

<b>SportObjects.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/66b4095d-4017-487d-8d23-4e24f73ebfc7)
<br>
2. Ввиду сложности данных в исходной таблице, парсинг был осуществлен без помощи opencsv,<br> с использованием регулярных выражений и отдельных особенностей данной таблицы:<br>
<b>ParserCSV.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/741e2874-0023-4cbc-be65-79430fb90800)

3. Для работы с SQLite был создан отдельный класс DatabaseConnect.java<br>
Таблица была создана при помощи отдельных запросов<br>
Были учтены подсказки из предпоследней лекции, по закрытию resultset,а также соединения с базой данных<br>
<b>DatabaseConnect.java</b>:<br>
'
    public class DatabaseConnect {
        private final Connection connection;
        public void close() throws SQLException {
            connection.close();
        }
    
        public DatabaseConnect() throws SQLException, ClassNotFoundException {
            Class.forName("org.sqlite.JDBC");
    
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
    
            connection = DriverManager.getConnection("jdbc:sqlite:sportObjects.db", config.toProperties());
    
            createTables();
        }
    
        private void createTables() throws SQLException{
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS Regions" +
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
    
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS SportObjects ("
                            + "objectId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "name TEXT NOT NULL,"
                            + "address TEXT NOT NULL,"
                            + "date TEXT NOT NULL,"
                            + "regionId INTEGER,"
                            + "FOREIGN KEY (regionId) REFERENCES Regions(id));");
        }
    
        public List<String> getAllData() throws SQLException {
            List<String> allData = new ArrayList<>();
    
            String sql = """
                    SELECT
                        SportObjects.name, Regions.name AS region,
                        SportObjects.address, SportObjects.date
                    FROM `SportObjects`
                        LEFT JOIN `Regions`
                            ON Regions.id = SportObjects.regionId
                    """;
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet data = preparedStatement.executeQuery()) {
                while (data.next()) {
                    String dbLine = data.getString("name") + ", " +
                            data.getString("region") + ", " +
                            data.getString("address") + ", " +
                            data.getString("date");
    
                    allData.add(dbLine);
                }
            }
            return allData;
        }
    
        public void insertRegion(Regions region) throws SQLException {
            if (!regionExists(region.getRegion())) {
                String sqlInsertTeam = "INSERT INTO Regions(name) VALUES(?)";
    
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertTeam)) {
                    preparedStatement.setString(1, region.getRegion());
                    preparedStatement.executeUpdate();
                }
            }
    
            for (SportObjects sportObjects : region.getObjects()) {
                insertObject(sportObjects, getRegionId(region.getRegion()));
            }
        }
    
        private boolean regionExists(String region) throws SQLException {
            String sql = "SELECT COUNT(*) FROM Regions WHERE name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.getInt(1) > 0;
            }
        }
    
        private int getRegionId(String region) throws SQLException {
            String sql = "SELECT id FROM Regions WHERE name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.getInt("id");
            }
        }
    
        public void insertObject(SportObjects sportObjects, int regionId) throws SQLException {
            String sql = "INSERT INTO SportObjects(name, address, date, regionId) VALUES (?, ?, ?, ?)";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, sportObjects.getName());
                preparedStatement.setString(2,sportObjects.getAddress());
                preparedStatement.setString(3, sportObjects.getDate());
                preparedStatement.setInt(4, regionId);
                preparedStatement.executeUpdate();
            }
        }
'<br>
4. Для работы с заданиями и со всеми классами был создан класс Main.java и его методы:<br>
<b>Main.java:</b><br>
'
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
            
                        // Задание 1: Построить гистограмму объектов по регионам
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
                    System.out.println("1. Гистограмма построена");
                }
            
                // Для вывода среднего кол-ва объектов в регионах
                private static void displayCountAvg() throws SQLException {
                    System.out.println("2. Среднее кол-во объектов спорта в регионах: " + dataBase.getCountAvg());
                }
            
                // Для вывода топ-3 регионов по кол-ву спортивных объектов
                private static void displayCountObj() throws SQLException {
                    System.out.println("3. Топ-3 региона с наибольшим количеством спортивных объектов: ");
                    System.out.println(String.join(", ", dataBase.getCountObj()));
                }
            }

'

# Выполнение заданий<br>
Осуществляем запрос к бд через класс DatabaseConnect.java и проверяем работоспособность в Main.java:<br>
<b>Задание №1</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/57b80eb0-75a9-4c2d-b795-771756f98295)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/780d6720-69ef-47cb-b04f-52a20aac4b16)<br>
<b>Задание №2</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/ddfd4097-ef17-4c45-8197-45352e5e0e69)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/83ab4c25-398c-4066-9ab4-a282d1390fd2)<br>
<b>Задание №3</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/64fdf3a5-982c-481b-b599-3f15f370d19f)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/82bc7a36-c144-4e76-aa06-e20cc5807770)<br>
# Результаты работы программы
![graphic](https://github.com/koxeiri/RTFjava/assets/155970191/007411cf-34ba-4a7c-8c3d-bded295d37ba)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/3524275b-1feb-42ad-a9f0-9b9fc53644f9)



