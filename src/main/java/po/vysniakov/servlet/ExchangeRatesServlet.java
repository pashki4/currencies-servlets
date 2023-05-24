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
import java.math.BigDecimal;
import java.util.*;
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
                    " targetCurrencyCode, rate"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        }

        String baseCurrencyCode = bodyParameters.get("baseCurrencyCode");
        CurrencyRepository currencyRepository = new JDBCCurrencyRepository();
        Optional<Currency> baseCurrency = currencyRepository.findByCode(baseCurrencyCode);
        if (baseCurrency.isEmpty()) {
            String json = new Gson().toJson(new Message("Currency: " + baseCurrencyCode + " does not exist"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
        }

        String targetCurrencyCode = bodyParameters.get("targetCurrencyCode");
        Optional<Currency> targetCurrency = currencyRepository.findByCode(targetCurrencyCode);
        if (targetCurrency.isEmpty()) {
            String json = new Gson().toJson(new Message("Currency: " + targetCurrencyCode + " does not exist"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
        }

        bodyParameters.get("rate"); //Convert to bigdecimal/double
        ExchangeRate newExchangeRate = new ExchangeRate();
        //TODO implement me
        CurrencyRepository currencyRepository = new JDBCCurrencyRepository();
        ExchangeRepository repository = new JDBCExchangeRepository();

    }

    private Map<String, String> convertToMap(List<String> bodyParameters) {
        return bodyParameters.stream()
                .map(str -> str.split("&"))
                .map(Arrays::asList)
                .flatMap(Collection::stream)
                .map(e -> e.split("="))
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));
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
        return keys.containsAll(requiredParameters);
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
