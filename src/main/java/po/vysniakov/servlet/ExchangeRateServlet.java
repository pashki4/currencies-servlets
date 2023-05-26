package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
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
import po.vysniakov.repositories.JDBCCurrencyRepository;
import po.vysniakov.repositories.JDBCExchangeRepository;
import po.vysniakov.repositories.ExchangeRepository;
import po.vysniakov.util.ExchangeRateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        Optional<String> requestedCurrencyPair = getRequestedCurrencyPair(req);
        if (requestedCurrencyPair.isEmpty() || requestedCurrencyPair.get().length() != 6) {
            String message = new Gson().toJson(new Message("Bad request"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        ExchangeRepository exchangeRepository = new JDBCExchangeRepository();
        Optional<ExchangeRate> rate = exchangeRepository.findPairByCode(requestedCurrencyPair.get());
        if (rate.isEmpty()) {
            String message = new Gson().toJson(new Message("The exchange rate was not found"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
            return;
        }

        String json = new Gson().toJson(rate.get());
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
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

    private Optional<String> getRequestedCurrencyPair(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String[] split = pathInfo.split("/");
        if (split.length != 0) {
            String pair = split[split.length - 1];
            return Optional.of(pair);
        }
        return Optional.empty();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPath(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    private void doPath(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE);
        Optional<String> requestedCurrencyPair = getRequestedCurrencyPair(req);
        if (requestedCurrencyPair.isEmpty() || requestedCurrencyPair.get().length() != 6) {
            String message = new Gson().toJson(new Message("Bad request"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        Map<String, String> bodyParameters = getBodyParameters(req);
        if (!validateParameters(bodyParameters)) {
            String message = new Gson().toJson(new Message("You need to put parameter rate (must be positive digit)"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        ExchangeRepository exchangeRateRepository = new JDBCExchangeRepository();
        Optional<ExchangeRate> maybeExchangeRate =
                exchangeRateRepository.findPairByCode(requestedCurrencyPair.get());
        if (maybeExchangeRate.isEmpty()) {
            String message = new Gson()
                    .toJson(new Message("Currency pair:" + requestedCurrencyPair.get() + " does not exist"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
            return;
        }

        Double rate = Double.valueOf(bodyParameters.get("rate"));
        ExchangeRate exchangeRate = maybeExchangeRate.get();
        exchangeRate.setRate(rate);
        try {
            exchangeRateRepository.updateRate(exchangeRate);
            String json = new Gson().toJson(exchangeRate);
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
        } catch (RuntimeException e) {
            String message = new Gson().toJson(new Message(e.getMessage()));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
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
        List<String> requiredParameters = List.of("rate");
        Set<String> keys = params.keySet();
        if (keys.size() != 1) {
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
}
