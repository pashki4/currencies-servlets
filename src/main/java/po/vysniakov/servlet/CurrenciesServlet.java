package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.currencie.dao.Currency;
import po.vysniakov.db.DatabaseManager;
import po.vysniakov.db.SQLiteDatabaseManager;

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
        DatabaseManager databaseManager = new SQLiteDatabaseManager();
        List<Currency> currencies = databaseManager.findAll();
        String json = new Gson().toJson(currencies);
        writeJsonToResponse(resp, json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String[] params = readBodyParameters(req);
        if (!validateParameters(params)) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "You need to put 3 parameters");
        } else {
            DatabaseManager databaseManager = new SQLiteDatabaseManager();
            Currency newCurrency = createCurrencyFromParameters(params);
            try {
                Currency savedCurrency = databaseManager.save(newCurrency);
                String json = new Gson().toJson(savedCurrency);
                writeJsonToResponse(resp, json);
            } catch (RuntimeException e) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "Cannot add currency, with same code");
            }
        }
    }

    private static void sendError(HttpServletResponse resp, int code, String message) {
        try {
            resp.sendError(code, message);
        } catch (IOException e) {
            throw new RuntimeException("Exception while sending error through response", e);
        }
    }

    private String[] readBodyParameters(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            String collected = reader.lines().collect(Collectors.joining());
            return collected.split("&");
        } catch (IOException e) {
            throw new RuntimeException("Cannot get reader for request: " + req, e);
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
            throw new RuntimeException("Cannot get print writer", e);
        }
    }
}
