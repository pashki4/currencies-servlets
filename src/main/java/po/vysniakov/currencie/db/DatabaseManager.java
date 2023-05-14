package po.vysniakov.currencie.db;

import po.vysniakov.currencie.Currency;

import java.util.List;

public interface DatabaseManager {
    List<Currency> selectAll();
}
