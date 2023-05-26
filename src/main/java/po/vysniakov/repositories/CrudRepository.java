package po.vysniakov.repositories;

import java.util.List;

public interface CrudRepository<T> {
    List<T> findAll();

    T save(T entity);
}
