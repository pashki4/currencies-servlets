package po.vysniakov.db;

import po.vysniakov.currencie.dao.Currency;

import java.util.List;
import java.util.Optional;

public interface DatabaseManager {
    List<Currency> findAll();
    Optional<Currency> findOne(String name);
}
