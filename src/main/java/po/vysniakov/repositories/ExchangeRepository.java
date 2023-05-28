package po.vysniakov.repositories;

import po.vysniakov.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRepository extends CrudRepository<ExchangeRate>{
    Optional<ExchangeRate> findPairByCode(String pair);

    void updateRate(ExchangeRate exchangeRate);


}
