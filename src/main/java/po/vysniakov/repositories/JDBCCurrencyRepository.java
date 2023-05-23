package po.vysniakov.repositories;

import org.sqlite.SQLiteConfig;
import po.vysniakov.exception.RepositoryOperationException;
import po.vysniakov.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static po.vysniakov.util.PropertiesUtil.get;

public class JDBCCurrencyRepository implements CurrencyRepository {

    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String FIND_CURRENCIES_SQL = "SELECT * FROM currencies;";
    private static final String FIND_CURRENCY_SQL = "SELECT * FROM currencies WHERE code = ?;";
    private static final String INSERT_CURRENCY_SQL = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?);";
    private static final String CONNECTION_EXCEPTION = "Cannot create connection for URL: ";

    @Override
    public List<Currency> findAll() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement preparedFindStatement = prepareFindAllStatement(connection);
            return getCurrencies(preparedFindStatement);
        } catch (SQLException e) {
            throw new RepositoryOperationException(CONNECTION_EXCEPTION + URL, e);
        }
    }


    private static SQLiteConfig getSqLiteConfig() {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return config;
    }

    private PreparedStatement prepareFindAllStatement(Connection connection) {
        try {
            return connection.prepareStatement(FIND_CURRENCIES_SQL);
        } catch (SQLException e) {
            throw new RepositoryOperationException("Cannot prepare find statement for: " + FIND_CURRENCIES_SQL, e);
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
    public Optional<Currency> findByCode(String code) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(URL)) {
            return findCurrency(connection, code);
        } catch (SQLException e) {
            throw new RepositoryOperationException(CONNECTION_EXCEPTION + URL, e);
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
            throw new RepositoryOperationException("Cannot prepare find statement for query: " + FIND_CURRENCY_SQL, e);
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
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        SQLiteConfig config = getSqLiteConfig();
        try (Connection connection = DriverManager.getConnection(URL, config.toProperties())) {
            return saveCurrency(connection, currency);
        } catch (SQLException e) {
            throw new RepositoryOperationException(CONNECTION_EXCEPTION + URL, e);
        }
    }

    private Currency saveCurrency(Connection connection, Currency currency) {
        try {
            PreparedStatement prepareInsertStatement = connection.prepareStatement(INSERT_CURRENCY_SQL,
                    Statement.RETURN_GENERATED_KEYS);
            fillInsertStatement(prepareInsertStatement, currency);
            prepareInsertStatement.executeUpdate();
            Long generatedKey = extractGeneratedKey(prepareInsertStatement);
            currency.setId(generatedKey);
            return currency;
        } catch (SQLException e) {
            throw new RepositoryOperationException(e.getMessage(), e.getCause());
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
