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
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.JDBCExchangeRepository;
import po.vysniakov.repositories.ExchangeRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        String[] bodyParameters = readBodyParameters(req);
        if (!validateParameters(bodyParameters)) {
            String json = new Gson().toJson(new Message("You need to put 3 parameters: baseCurrencyCode," +
                    " targetCurrencyCode, rate"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        }
        CrudRepository<ExchangeRate> repository = new JDBCExchangeRepository();
        //TODO implement me

    }

    private String[] readBodyParameters(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            String collected = reader.lines().collect(Collectors.joining());
            return collected.split("&");
        } catch (IOException e) {
            throw new ExchangeRatesServletOperationException("Cannot get reader for request: " + req, e);
        }
    }

    private boolean validateParameters(String[] params) {
        List<String> paramList = Arrays.asList("baseCurrencyCode", "targetCurrencyCode", "rate");
        if (params.length != 3) {
            return false;
        }
        return Arrays.asList(params).containsAll(paramList);
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
