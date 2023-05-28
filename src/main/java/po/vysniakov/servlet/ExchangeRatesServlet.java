package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.exception.ExchangeRatesServletOperationException;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.model.Message;
import po.vysniakov.repositories.ExchangeRepository;
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
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final List<String> REQUIRED_PARAMETERS = List.of("baseCurrencyCode", "targetCurrencyCode", "rate");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        ExchangeRepository repository = new JDBCExchangeRepository();
        List<ExchangeRate> exchangeRates = repository.findAll();
        String json = new Gson().toJson(exchangeRates);
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);

        Map<String, String> bodyParameters = getBodyParameters(req);
        if (!validateParameters(bodyParameters)) {
            String message = new Gson().toJson(new Message("You need to put 3 parameters: " +
                    String.join(", ", REQUIRED_PARAMETERS) + " (rate must be positive digit)"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        ExchangeRepository exchangeRepository = new JDBCExchangeRepository();
        String baseCurrencyCode = bodyParameters.get("baseCurrencyCode");
        String targetCurrencyCode = bodyParameters.get("targetCurrencyCode");
        Optional<ExchangeRate> maybeExchangeRate = exchangeRepository.findPairByCode(baseCurrencyCode + targetCurrencyCode);
        if (maybeExchangeRate.isEmpty()) {
            String message = new Gson().toJson(new Message("Currency pair does not exist: " + baseCurrencyCode + targetCurrencyCode));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        Double rate = Double.valueOf(bodyParameters.get("rate"));
        ExchangeRate exchangeRate = maybeExchangeRate.get();
        exchangeRate.setRate(rate);

        ExchangeRepository exchangeRateRepository = new JDBCExchangeRepository();
        try {
            ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
            String json = new Gson().toJson(saved);
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
        } catch (RuntimeException e) {
            String message = new Gson().toJson(new Message(e.getMessage()));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_CONFLICT, message);
        }
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
        Set<String> keys = params.keySet();
        if (keys.size() != 3) {
            return false;
        }

        if (!keys.containsAll(REQUIRED_PARAMETERS)) {
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
