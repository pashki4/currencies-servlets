package po.vysniakov.repositories;

import org.sqlite.SQLiteConfig;
import po.vysniakov.exception.ExchangeRatesServletOperationException;
import po.vysniakov.exception.RepositoryOperationException;
import po.vysniakov.model.Currency;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.util.ExchangeRateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static po.vysniakov.util.PropertiesUtil.get;

public class JDBCExchangeRepository implements ExchangeRepository {
    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String CONNECTION_EXCEPTION = "Cannot create connection for URL: ";
    private static final String FIND_EXCHANGE_RATES_SQL = "SELECT b.id, b.code, b.full_name, b.sign, " +
            "t.id, t.code, t.full_name, t.sign, er.rate " +
            "FROM ExchangeRates er " +
            "LEFT JOIN currencies b ON er.base_currency_id = b.id " +
            "LEFT JOIN currencies t ON er.target_currency_id = t.id;";

    private static final String FIND_EXCHANGE_RATE_SQL = "SELECT b.id, b.code, b.full_name, b.sign, " +
            "t.id, t.code, t.full_name, t.sign, er.rate " +
            "FROM ExchangeRates er " +
            "LEFT JOIN currencies b ON er.base_currency_id = b.id " +
            "LEFT JOIN currencies t ON er.target_currency_id = t.id " +
            "WHERE b.code = ? AND t.code = ?";

    private static final String INSERT_EXCHANGE_RATE_SQL = "INSERT INTO ExchangeRates " +
            "(base_currency_id, target_currency_id, rate) " +
            "VALUES (?, ?, ?);";

    @Override
    public List<ExchangeRate> findAll() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement findAllStatement = prepareFindAllStatement(connection);
            ResultSet resultSet = findAllStatement.executeQuery();
            return collectToList(resultSet);
        } catch (SQLException e) {
            throw new RepositoryOperationException(CONNECTION_EXCEPTION + " " + URL, e);
        }
    }

    private List<ExchangeRate> collectToList(ResultSet resultSet) throws SQLException {
        List<ExchangeRate> result = new ArrayList<>();
        while (resultSet.next()) {
            Optional<ExchangeRate> rate = parseExchangeRate(resultSet);
            rate.ifPresent(result::add);
        }
        return result;
    }

    private PreparedStatement prepareFindAllStatement(Connection connection) {
        try {
            return connection.prepareStatement(FIND_EXCHANGE_RATES_SQL);
        } catch (SQLException e) {
            throw new RepositoryOperationException("Cannot prepare find statement for: " + FIND_EXCHANGE_RATES_SQL, e);
        }
    }

    @Override
    public Optional<ExchangeRate> findPair(String pair) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(URL)) {
            return findExchangeRate(connection, pair);
        } catch (SQLException e) {
            throw new RepositoryOperationException("Cannot create connection by URL: " + URL, e);
        }
    }

    private Optional<ExchangeRate> findExchangeRate(Connection connection, String pair) {
        try {
            PreparedStatement findExchangeRateStatement = connection.prepareStatement(FIND_EXCHANGE_RATE_SQL);
            fillFindExchangeRateStatement(findExchangeRateStatement, pair);
            ResultSet resultSet = findExchangeRateStatement.executeQuery();
            if (resultSet.next()) {
                return parseExchangeRate(resultSet);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryOperationException("Cannot prepare findExchangeRate statement", e);
        }
    }

    private void fillFindExchangeRateStatement(PreparedStatement preparedStatement, String pair) throws SQLException {
        List<String> pairs = ExchangeRateUtil.splitPairByCode(pair);
        preparedStatement.setString(1, pairs.get(0));
        preparedStatement.setString(2, pairs.get(1));
    }

    private Optional<ExchangeRate> parseExchangeRate(ResultSet resultSet) throws SQLException {
        ExchangeRate exchangeRate = new ExchangeRate();

        Currency baseCurrency = new Currency();
        baseCurrency.setId(resultSet.getLong(1));
        baseCurrency.setCode(resultSet.getString(2));
        baseCurrency.setName(resultSet.getString(3));
        baseCurrency.setSign(resultSet.getString(4));

        Currency targetCurrency = new Currency();
        targetCurrency.setId(resultSet.getLong(5));
        targetCurrency.setCode(resultSet.getString(6));
        targetCurrency.setName(resultSet.getString(7));
        targetCurrency.setSign(resultSet.getString(8));

        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(resultSet.getDouble(9));
        return Optional.of(exchangeRate);
    }

    @Override
    public ExchangeRate save(ExchangeRate entity) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        SQLiteConfig config = getSqLiteConfig();
        try (Connection connection = DriverManager.getConnection(URL, config.toProperties())) {
            return saveExchangeRate(connection, entity);
        } catch (SQLException e) {
            throw new ExchangeRatesServletOperationException(CONNECTION_EXCEPTION + URL, e);
        }
    }

    private ExchangeRate saveExchangeRate(Connection connection, ExchangeRate entity) {
        try {
            PreparedStatement savePrepareStatement = connection.prepareStatement(INSERT_EXCHANGE_RATE_SQL,
                    Statement.RETURN_GENERATED_KEYS);
            fillSavePrepareStatement(savePrepareStatement, entity);
            savePrepareStatement.executeUpdate();
            Long generatedId = extractGeneratedKey(savePrepareStatement);
            entity.setId(generatedId);
            return entity;
        } catch (SQLException e) {
            throw new ExchangeRatesServletOperationException(e.getMessage(), e);
        }
    }

    private static Long extractGeneratedKey(PreparedStatement prepareInsertStatement) throws SQLException {
        ResultSet generatedKey = prepareInsertStatement.getGeneratedKeys();
        if (generatedKey.next()) {
            return generatedKey.getLong(1);
        }
        return null;
    }

    private void fillSavePrepareStatement(PreparedStatement savePrepareStatement, ExchangeRate entity) throws SQLException {
        savePrepareStatement.setLong(1, entity.getBaseCurrency().getId());
        savePrepareStatement.setLong(2, entity.getTargetCurrency().getId());
        savePrepareStatement.setDouble(3, entity.getRate());
    }

    private static SQLiteConfig getSqLiteConfig() {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return config;
    }
}
