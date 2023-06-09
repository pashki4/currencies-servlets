package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.CurrenciesServletOperationException;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.model.Currency;
import po.vysniakov.model.Message;
import po.vysniakov.repositories.CurrencyRepository;
import po.vysniakov.repositories.JDBCCurrencyRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        CurrencyRepository currencyRepository = new JDBCCurrencyRepository();
        List<Currency> currencies = currencyRepository.findAll();

        String json = new Gson().toJson(currencies);
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        String[] bodyParameters = readBodyParameters(req);

        if (!validateParameters(bodyParameters)) {
            String json = new Gson().toJson(new Message("You need to put 3 parameters: code, name, sign"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        }

        Currency newCurrency = createCurrencyFromParameters(bodyParameters);
        CurrencyRepository currencyRepository = new JDBCCurrencyRepository();
        try {
            Currency savedCurrency = currencyRepository.save(newCurrency);
            String json = new Gson().toJson(savedCurrency);
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
        } catch (RuntimeException e) {
            String message = new Gson().toJson(new Message(e.getMessage()));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_CONFLICT, message);
        }
    }

    private String[] readBodyParameters(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            String collected = reader.lines().collect(Collectors.joining());
            return collected.split("&");
        } catch (IOException e) {
            throw new CurrenciesServletOperationException("Cannot get reader for request: " + req, e);
        }
    }

    private Currency createCurrencyFromParameters(String[] params) {
        Currency newCurrency = new Currency();
        for (String current : params) {
            String[] keyValuePair = current.split("=");
            switch (keyValuePair[0].toLowerCase()) {
                case "code" -> newCurrency.setCode(keyValuePair[1]);
                case "name" -> newCurrency.setName(keyValuePair[1]);
                case "sign" -> newCurrency.setSign(keyValuePair[1]);
            }
        }
        return newCurrency;
    }

    private boolean validateParameters(String[] params) {
        List<String> paramList = Arrays.asList("code", "name", "sign");
        if (params.length != 3) {
            return false;
        }
        List<String> split = Arrays.stream(params)
                .flatMap(param -> Stream.of(param.split("=")))
                .toList();

        return split.containsAll(paramList);
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
