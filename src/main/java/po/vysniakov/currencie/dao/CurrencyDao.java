package po.vysniakov.currencie.dao;

import po.vysniakov.db.DatabaseManager;

import java.util.List;

public class CurrencyDao implements Dao<Currency> {

    private DatabaseManager databaseManager;

    public CurrencyDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public List<Currency> getAll() {
        return databaseManager.findAll();
    }
}
