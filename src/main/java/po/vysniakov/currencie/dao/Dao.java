package po.vysniakov.currencie.dao;

import java.util.List;

public interface Dao<T> {

    List<T> getAll();

}
