package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.CurrenciesServletOperationException;
import po.vysniakov.model.Currency;
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.CurrencyRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        CrudRepository<Currency> currencyRepository = new CurrencyRepository();
        List<Currency> currencies = currencyRepository.findAll();
        String json = new Gson().toJson(currencies);
        writeJsonToResponse(resp, json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String[] bodyParameters = readBodyParameters(req);

        if (!validateParameters(bodyParameters)) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "You need to put 3 parameters");
            return;
        }

        CrudRepository<Currency> currencyRepository = new CurrencyRepository();
        Currency newCurrency = createCurrencyFromParameters(bodyParameters);
        try {
            Currency savedCurrency = currencyRepository.save(newCurrency);
            String json = new Gson().toJson(savedCurrency);
            writeJsonToResponse(resp, json);
        } catch (RuntimeException e) {
            sendError(resp, HttpServletResponse.SC_CONFLICT, "Code: " + newCurrency.getCode() + " exists");
        }
    }

    private static void sendError(HttpServletResponse resp, int code, String message) {
        try {
            resp.sendError(code, message);
        } catch (IOException e) {
            throw new CurrenciesServletOperationException("Exception while sending error through response", e);
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
        return params.length == 3;
    }

    private static void writeJsonToResponse(HttpServletResponse resp, String json) {
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(json);
            writer.flush();
        } catch (IOException e) {
            throw new CurrenciesServletOperationException("Cannot get print writer", e);
        }
    }
}
