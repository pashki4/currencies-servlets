package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.model.Exchange;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.model.Message;
import po.vysniakov.repositories.ExchangeRepository;
import po.vysniakov.repositories.JDBCExchangeRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final List<String> REQUIRED_REQUEST_BODY_PARAMS = List.of("from", "to", "amount");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        Map<String, String> parameters = getRequestBodyParams(req);
        if (!validateRequestParam(parameters)) {
            String message = new Gson().toJson(new Message("You need to put 3 parameters: " +
                    String.join(", ", REQUIRED_REQUEST_BODY_PARAMS) +
                    " (amount must be positive digit)"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        ExchangeRepository exchangeRepository = new JDBCExchangeRepository();
        Optional<ExchangeRate> exchangeRate =
                exchangeRepository.findPairByCode(parameters.get("from") + parameters.get("to"))
                        .or(() -> reverseSearch(parameters.get("from") + parameters.get("to"), exchangeRepository))
                        .or(() -> usdBaseSearch(parameters.get("from") + parameters.get("to"), exchangeRepository));

        if (exchangeRate.isEmpty()) {
            String message = new Gson().toJson(new Message("There is no data for currency pair: " +
                    parameters.get("from") + parameters.get("to")));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
            return;
        }
        String amount = parameters.get("amount");
        String json = new Gson().toJson(createExchange(exchangeRate.get(), amount));
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    private Optional<? extends ExchangeRate> reverseSearch(String pair, ExchangeRepository exchangeRepository) {
        String baseCurrencyCode = pair.substring(3);
        String targetCurrencyCode = pair.substring(0, 3);
        Optional<ExchangeRate> exchangeRate = exchangeRepository.findPairByCode(baseCurrencyCode + targetCurrencyCode);
        if(exchangeRate.isPresent()) {
            return reverse(exchangeRate.get());
        }
        return Optional.empty();
    }

    private Optional<? extends ExchangeRate> reverse(ExchangeRate exchangeRate) {
        ExchangeRate reversed = new ExchangeRate();
        reversed.setRate(1 / exchangeRate.getRate());
        reversed.setBaseCurrency(exchangeRate.getTargetCurrency());
        reversed.setTargetCurrency(exchangeRate.getBaseCurrency());
        return Optional.of(reversed);
    }

    private Optional<ExchangeRate> usdBaseSearch(String pair, ExchangeRepository exchangeRepository) {
        String baseCurrencyCode = pair.substring(0, 3);
        String targetCurrencyCode = pair.substring(3);
        Optional<ExchangeRate> baseToUsd = exchangeRepository.findPairByCode(baseCurrencyCode + "USD");
        if (baseToUsd.isEmpty()) {
            return Optional.empty();
        }
        Optional<ExchangeRate> targetToUsd = exchangeRepository.findPairByCode(targetCurrencyCode + "USD");
        if (targetToUsd.isEmpty()) {
            return Optional.empty();
        }
        Double exchangeRate = getExchangeRate(baseToUsd.get().getRate(), targetToUsd.get().getRate());
        ExchangeRate result = new ExchangeRate();
        result.setRate(exchangeRate);
        result.setBaseCurrency(baseToUsd.get().getBaseCurrency());
        result.setTargetCurrency((targetToUsd.get().getBaseCurrency()));
        return Optional.of(result);
    }

    private Exchange createExchange(ExchangeRate exchangeRate, String amount) {
        BigDecimal convertedAmount = BigDecimal.valueOf(Double.parseDouble(amount))
                .multiply(BigDecimal.valueOf(exchangeRate.getRate()))
                .setScale(2, RoundingMode.HALF_EVEN);
        Exchange result = new Exchange();
        result.setConvertedAmount(convertedAmount);
        result.setRate(exchangeRate.getRate());
        result.setAmount(Double.valueOf(amount));
        result.setBaseCurrency(exchangeRate.getBaseCurrency());
        result.setTargetCurrency(exchangeRate.getTargetCurrency());
        return result;
    }

    private Double getExchangeRate(Double baseRate, Double targetRate) {
        Double baseUpsideDown = 1 / baseRate;
        Double targetUpsideDown = 1 / targetRate;
        return targetUpsideDown / baseUpsideDown;
    }

    private boolean validateRequestParam(Map<String, String> parameters) {
        if (parameters.keySet().size() != 3) {
            return false;
        }
        if (!parameters.keySet().containsAll(REQUIRED_REQUEST_BODY_PARAMS)) {
            return false;
        }
        String amount = parameters.get("amount");
        return isDigit(amount);
    }

    private boolean isDigit(String value) {
        if (value == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        return pattern.matcher(value).matches();
    }

    private static Map<String, String> getRequestBodyParams(HttpServletRequest req) {
        return req.getParameterMap()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> String.join("", entry.getValue())));
    }

    private void setCodeAndJsonToResponse(HttpServletResponse resp, int code, String json) {
        resp.setStatus(code);
        try {
            PrintWriter writer = resp.getWriter();
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            throw new ExchangeRateServletOperationException("Cannot get response writer", e);
        }
    }
}
