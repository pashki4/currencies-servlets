package po.vysniakov.repositories;

import po.vysniakov.exception.ConnectionException;
import po.vysniakov.exception.PrepareStatementException;
import po.vysniakov.model.Currency;
import po.vysniakov.model.ExchangeRate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static po.vysniakov.util.PropertiesUtil.get;

public class ExchangeRateRepository implements CrudRepository<ExchangeRate> {
    private static final String URL = "jdbc:sqlite:" + get("db.url") + get("db.name");
    private static final String FIND_EXCHANGE_RATES_SQL = "SELECT b.id, b.code, b.full_name, b.sign, " +
            "t.id, t.code, t.full_name, t.sign, er.rate " +
            "FROM ExchangeRates er " +
            "LEFT JOIN currencies b ON er.base_currency_id = b.id " +
            "LEFT JOIN currencies t ON er.target_currency_id = t.id;";

    @Override
    public List<ExchangeRate> findAll() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            PreparedStatement findAllStatement = prepareFindAllStatement(connection);
            ResultSet resultSet = findAllStatement.executeQuery();
            return collectToList(resultSet);
        } catch (SQLException e) {
            throw new ConnectionException("Cannot create connection for URL: " + URL, e);
        }
    }

    private List<ExchangeRate> collectToList(ResultSet resultSet) throws SQLException {
        List<ExchangeRate> result = new ArrayList<>();
        while (resultSet.next()) {
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
            result.add(exchangeRate);
        }
        return result;
    }

    private PreparedStatement prepareFindAllStatement(Connection connection) {
        try {
            return connection.prepareStatement(FIND_EXCHANGE_RATES_SQL);
        } catch (SQLException e) {
            throw new PrepareStatementException("Cannot prepare find statement for: " + FIND_EXCHANGE_RATES_SQL, e);
        }
    }

    @Override
    public Optional<ExchangeRate> findOne(String name) {
        return Optional.empty();
    }

    @Override
    public ExchangeRate save(Currency currency) {
        return null;
    }
}
