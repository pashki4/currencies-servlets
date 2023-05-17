package po.vysniakov.repositories;

import po.vysniakov.model.Currency;
import po.vysniakov.model.ExchangeRate;

import java.util.List;
import java.util.Optional;

public class ExchangeRateRepository implements CrudRepository<ExchangeRate> {
    private static final String SELECT_EXCHANGE_RATES_SQL =
            "SELECT b.id, b.code, b.full_name, b.sign, t.id, t.code, t.full_name, t.sign, er.rate " +
                    "FROM currencies b " +
                    "LEFT JOIN ExchangeRates er ON b.id = er.base_currency_id " +
                    "LEFT JOIN currencies t ON t.id = er.target_currency_id;";

    @Override
    public List<ExchangeRate> findAll() {
        return null;
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
