package po.vysniakov;

import java.sql.*;

public class SQLite {
    private static final String URL = "jdbc:sqlite:D:/Dev/sqlite/db/mydb";
    private static final String SELECT_ALL_SQL = "SELECT * FROM currencies;";

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("id: " + resultSet.getInt("id") +
                        " code: " + resultSet.getString("code") +
                        " full name: " + resultSet.getString("full_name")
                );
            }
        }
    }
}
