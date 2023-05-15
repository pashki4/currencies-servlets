package po.vysniakov.db;

import po.vysniakov.currencie.dao.Currency;
import po.vysniakov.exception.ConnectionException;
import po.vysniakov.exception.PrepareStatementException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static po.vysniakov.util.PropertiesUtil.get;

public class SQLiteDatabaseManager implements DatabaseManager {

    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String SELECT_ALL_SQL = "SELECT * FROM currencies;";
    private static final String SELECT_ONE_SQL = "SELECT * FROM currencies WHERE code = ?;";

    @Override
    public List<Currency> findAll() {
        return findCurrencies();
    }

    private List<Currency> findCurrencies() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement preparedSelectStatement = prepareSelectAllStatement(connection);
            return getCurrencies(preparedSelectStatement);
        } catch (SQLException e) {
            throw new ConnectionException("Can't create connection for URL: " + URL, e);
        }
    }

    private PreparedStatement prepareSelectAllStatement(Connection connection) {
        try {
            return connection.prepareStatement(SELECT_ALL_SQL);
        } catch (SQLException e) {
            throw new PrepareStatementException("Can't prepare select statement for query: " + SELECT_ALL_SQL, e);
        }
    }

    private List<Currency> getCurrencies(PreparedStatement preparedSelectStatement) throws SQLException {
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

    @Override
    public Optional<Currency> findOne(String name) {
        try (Connection connection = DriverManager.getConnection(URL)) {
            return findCurrency(connection, name);
        } catch (SQLException e) {
            throw new ConnectionException("Can't create connection for URL: " + URL, e);
        }
    }

    private Optional<Currency> findCurrency(Connection connection, String code) throws SQLException {
        PreparedStatement preparedFindStatement = prepareSelectOneStatement(connection, code);
        return mapCurrency(preparedFindStatement);
    }

    private PreparedStatement prepareSelectOneStatement(Connection connection, String code) {
        try {
            PreparedStatement selectOneStatement = connection.prepareStatement(SELECT_ONE_SQL);
            fillSelectOneStatement(selectOneStatement, code);
            return selectOneStatement;
        } catch (SQLException e) {
            throw new PrepareStatementException("Can't prepare select statement for query: " + SELECT_ONE_SQL, e);
        }
    }

    private void fillSelectOneStatement(PreparedStatement selectOneStatement, String code) throws SQLException {
        selectOneStatement.setString(1, code);
    }

    private Optional<Currency> mapCurrency(PreparedStatement selectOneStatement) throws SQLException {
        ResultSet resultSet = selectOneStatement.executeQuery();
        if (resultSet.next()) {
            Currency currency = new Currency();
            currency.setId(resultSet.getLong("id"));
            currency.setCode(resultSet.getString("code"));
            currency.setName(resultSet.getString("full_name"));
            currency.setSign(resultSet.getString("sign"));
            return Optional.of(currency);
        }
        return Optional.empty();
    }
}
