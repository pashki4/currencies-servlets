package po.vysniakov.repositories;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    List<T> findAll();

    T save(T entity);
}
