package po.vysniakov.repositories;

import po.vysniakov.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRepository extends CrudRepository<ExchangeRate>{
    Optional<ExchangeRate> findPair(String pair);
}
