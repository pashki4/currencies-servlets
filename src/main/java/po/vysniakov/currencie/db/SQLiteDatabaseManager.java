package po.vysniakov.currencie.db;

import po.vysniakov.currencie.Currency;
import po.vysniakov.exception.GetConnectionException;
import po.vysniakov.exception.PrepareStatementException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static po.vysniakov.util.PropertiesUtil.get;

public class SQLiteDatabaseManager implements DatabaseManager {

    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String SELECT_SQL = "SELECT * FROM currencies;";

    @Override
    public List<Currency> selectAll() {
        return selectAllCurrencies();
    }

    private List<Currency> selectAllCurrencies() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement preparedSelectStatement = prepareSelectStatement(connection);
            return getResult(preparedSelectStatement);
        } catch (SQLException e) {
            throw new GetConnectionException("Can't create connection for URL: " + URL, e);
        }
    }

    private List<Currency> getResult(PreparedStatement preparedSelectStatement) throws SQLException {
        ResultSet resultSet = preparedSelectStatement.executeQuery();
        return collectToList(resultSet);
    }

    private List<Currency> collectToList(ResultSet resultSet) throws SQLException {
        List<Currency> result = new ArrayList<>();
        while (resultSet.next()) {
            Currency currency = new Currency();
            currency.setId(resultSet.getLong("id"));
            currency.setCode(resultSet.getString("code"));
            currency.setName(resultSet.getString("full_name"));
            currency.setSign(resultSet.getString("sign"));
            result.add(currency);
        }
        return result;
    }

    private PreparedStatement prepareSelectStatement(Connection connection) {
        try {
            return connection.prepareStatement(SELECT_SQL);
        } catch (SQLException e) {
            throw new PrepareStatementException("Can't prepare select statement for query: " + SELECT_SQL, e);
        }
    }
}
