package po.vysniakov.repositories;

import po.vysniakov.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    List<T> findAll();
    Optional<T> findOne(String name);

    T save(Currency currency);
}
