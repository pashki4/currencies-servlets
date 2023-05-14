package po.vysniakov.currencie.dao;

import po.vysniakov.currencie.Currency;

import java.util.List;

public interface Dao<T> {

    List<T> getAll();

}
