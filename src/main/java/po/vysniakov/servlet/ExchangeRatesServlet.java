package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.exception.ExchangeRatesServletOperationException;
import po.vysniakov.model.Currency;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.model.Message;
import po.vysniakov.repositories.CurrencyRepository;
import po.vysniakov.repositories.ExchangeRepository;
import po.vysniakov.repositories.JDBCCurrencyRepository;
import po.vysniakov.repositories.JDBCExchangeRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        ExchangeRepository repository = new JDBCExchangeRepository();
        List<ExchangeRate> exchangeRates = repository.findAll();
        String json = new Gson().toJson(exchangeRates);
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> bodyParameters = getBodyParameters(req);
        if (!validateParameters(bodyParameters)) {
            String json = new Gson().toJson(new Message("You need to put 3 parameters: baseCurrencyCode," +
                    " targetCurrencyCode, rate. Rate must be positive digit"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        }

        CurrencyRepository currencyRepository = new JDBCCurrencyRepository();
        String baseCurrencyCode = bodyParameters.get("baseCurrencyCode");
        Optional<Currency> baseCurrency = currencyRepository.findByCode(baseCurrencyCode);
        if (baseCurrency.isEmpty()) {
            String json2 = new Gson().toJson(new Message("Currency: " + baseCurrencyCode + " does not exist"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json2);
        }

        String targetCurrencyCode = bodyParameters.get("targetCurrencyCode");
        Optional<Currency> targetCurrency = currencyRepository.findByCode(targetCurrencyCode);
        if (targetCurrency.isEmpty()) {
            String json1 = new Gson().toJson(new Message("Currency: " + targetCurrencyCode + " does not exist"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json1);
        }

        Double rate = Double.valueOf(bodyParameters.get("rate"));
        ExchangeRate newExchangeRate = createExchangeRate(baseCurrency.get(), targetCurrency.get(), rate);

        ExchangeRepository exchangeRateRepository = new JDBCExchangeRepository();
        try {
            ExchangeRate savedExchangeRate = exchangeRateRepository.save(newExchangeRate);
            String json = new Gson().toJson(savedExchangeRate);
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
        } catch (RuntimeException e) {
            String message = new Gson().toJson(new Message(e.getMessage()));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_CONFLICT, message);
        }
    }

    private ExchangeRate createExchangeRate(Currency baseCurrency, Currency targetCurrency, Double rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(rate);
        return exchangeRate;
    }

    private Map<String, String> getBodyParameters(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            return reader.lines()
                    .flatMap(str -> Stream.of(str.split("&")))
                    .map(Arrays::asList)
                    .flatMap(Collection::stream)
                    .map(e -> e.split("="))
                    .collect(Collectors.toMap(e -> e[0], e -> e[1]));
        } catch (IOException e) {
            throw new ExchangeRatesServletOperationException("Cannot get reader for request: " + req, e);
        }
    }

    private boolean validateParameters(Map<String, String> params) {
        List<String> requiredParameters = Arrays.asList("baseCurrencyCode", "targetCurrencyCode", "rate");
        Set<String> keys = params.keySet();
        if (keys.size() != 3) {
            return false;
        }

        if (!keys.containsAll(requiredParameters)) {
            return false;
        }
        String rateValue = params.get("rate");
        return isDigit(rateValue);
    }

    private boolean isDigit(String value) {
        if (value == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        return pattern.matcher(value).matches();
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
