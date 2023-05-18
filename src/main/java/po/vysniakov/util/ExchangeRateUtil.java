package po.vysniakov.util;

import java.util.ArrayList;
import java.util.List;

public class ExchangeRateUtil {

    public static List<String> splitPairByCode(String pair) {
        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < pair.length()) {
            result.add(pair.substring(index, Math.min(index + 3, pair.length())));
            index += 4;
        }
        return result;
    }
}
