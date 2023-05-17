package po.vysniakov.repositories;

import po.vysniakov.model.Currency;
import po.vysniakov.exception.ConnectionException;
import po.vysniakov.exception.PrepareStatementException;
import po.vysniakov.exception.SaveException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static po.vysniakov.util.PropertiesUtil.get;

public class CurrencyRepository implements CrudRepository<Currency> {

    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String FIND_CURRENCIES_SQL = "SELECT * FROM currencies;";
    private static final String FIND_CURRENCY_SQL = "SELECT * FROM currencies WHERE code = ?;";
    private static final String INSERT_CURRENCY_SQL = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?);";

    @Override
    public List<Currency> findAll() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement preparedFindStatement = prepareFindAllStatement(connection);
            return getCurrencies(preparedFindStatement);
        } catch (SQLException e) {
            throw new ConnectionException("Cannot create connection for URL: " + URL, e);
        }
    }

    private PreparedStatement prepareFindAllStatement(Connection connection) {
        try {
            return connection.prepareStatement(FIND_CURRENCIES_SQL);
        } catch (SQLException e) {
            throw new PrepareStatementException("Cannot prepare find statement for: " + FIND_CURRENCIES_SQL, e);
        }
    }

    private List<Currency> getCurrencies(PreparedStatement preparedFindStatement) throws SQLException {
        ResultSet resultSet = preparedFindStatement.executeQuery();
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
            throw new ConnectionException("Cannot create connection for URL: " + URL, e);
        }
    }

    private Optional<Currency> findCurrency(Connection connection, String code) throws SQLException {
        PreparedStatement preparedFindStatement = prepareFindOneStatement(connection, code);
        return mapCurrency(preparedFindStatement);
    }

    private PreparedStatement prepareFindOneStatement(Connection connection, String code) {
        try {
            PreparedStatement findOneStatement = connection.prepareStatement(FIND_CURRENCY_SQL);
            fillFindOneStatement(findOneStatement, code);
            return findOneStatement;
        } catch (SQLException e) {
            throw new PrepareStatementException("Cannot prepare find statement for query: " + FIND_CURRENCY_SQL, e);
        }
    }

    private void fillFindOneStatement(PreparedStatement findOneStatement, String code) throws SQLException {
        findOneStatement.setString(1, code);
    }

    private Optional<Currency> mapCurrency(PreparedStatement findOneStatement) throws SQLException {
        ResultSet resultSet = findOneStatement.executeQuery();
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

    @Override
    public Currency save(Currency currency) {
        try (Connection connection = DriverManager.getConnection(URL)) {
            return saveCurrency(connection, currency);
        } catch (SQLException e) {
            throw new ConnectionException("Cannot create connection for URL: " + URL, e);
        }
    }

    private Currency saveCurrency(Connection connection, Currency currency) {
        try {
            PreparedStatement prepareInsertStatement = connection.prepareStatement(INSERT_CURRENCY_SQL,
                    Statement.RETURN_GENERATED_KEYS);
            fillInsertStatement(prepareInsertStatement, currency);
            int rows = prepareInsertStatement.executeUpdate();
            if (rows == 0) {
                throw new SaveException("Currency with code: " + currency.getCode()
                        + " already exists");
            } else {
                Long generatedKey = extractGeneratedKey(prepareInsertStatement);
                currency.setId(generatedKey);
                return currency;
            }
        } catch (SQLException e) {
            throw new PrepareStatementException("Cannot prepare statement for: " + INSERT_CURRENCY_SQL, e);
        }
    }

    private void fillInsertStatement(PreparedStatement prepareInsertStatement, Currency currency) throws SQLException {
        prepareInsertStatement.setString(1, currency.getCode());
        prepareInsertStatement.setString(2, currency.getName());
        prepareInsertStatement.setString(3, currency.getSign());
    }

    private static Long extractGeneratedKey(PreparedStatement prepareInsertStatement) throws SQLException {
        ResultSet generatedKey = prepareInsertStatement.getGeneratedKeys();
        if (generatedKey.next()) {
            return generatedKey.getLong(1);
        }
        return null;
    }
}
