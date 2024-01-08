# Итоговый проект по Java 3 вариант
1. Создадим классы Regions.java и SportObjects.java<br>
Regions.java - содержит данные о регионе и его спортивных объектах<br>
SportObjects.java - содержит данные непосредственно о спортивном объекте<br>
<b>Regions.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/088b1882-f176-40e8-8ad0-e849c1f88874)
<br>

<b>SportObjects.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/ad7d0eb7-5b75-44a3-9489-05afa6b17cb0)<br>
2. Ввиду сложности данных в исходной таблице, парсинг был осуществлен без помощи opencsv,<br> с использованием регулярных выражений и отдельных особенностей данной таблицы:<br>
<b>ParserCSV.java</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/c779168e-13a8-4e4a-b5ae-a7c2ec705b68)
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
    '

# Выполнение заданий<br>
Осуществляем запрос к бд через класс DatabaseConnect.java и проверяем работоспособность в Main.java:<br>
<b>Задание №1</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/738c9dcc-e712-4d29-b06b-f8dc8e1f1f3c)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/fa9c79a3-e93f-4e54-be7b-1bd763515e10)<br>
<b>Задание №2</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/50e26103-df75-465c-828e-8eeb62f11125)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/f4ed542a-e6b8-44bf-9744-7ae175c65262)<br>
<b>Задание №3</b><br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/c337e737-df3c-4728-aa47-b9ce117294a2)<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/d4f2d818-206a-49d8-9eff-a3025cb7ec93)<br>
# Результаты работы программы
![image](https://github.com/koxeiri/RTFjava/assets/155970191/742ef7cc-e39a-4f6d-9dad-b8122dc53297)
<br>
![image](https://github.com/koxeiri/RTFjava/assets/155970191/9591781d-29b4-42c6-b9b9-33311d788c85)

