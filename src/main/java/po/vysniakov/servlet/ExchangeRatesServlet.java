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
import po.vysniakov.repositories.ExchangeRepository;
import po.vysniakov.repositories.JDBCExchangeRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
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

        List<String> bodyParameters = readBodyParameters(req);
        if (!validateParameters(bodyParameters)) {
            String json = new Gson().toJson(new Message("You need to put 3 parameters: baseCurrencyCode," +
                    " targetCurrencyCode, rate"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        }
        CrudRepository<ExchangeRate> repository = new JDBCExchangeRepository();
        //TODO implement me

    }

    private List<String> readBodyParameters(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            return reader.lines()
                    .flatMap(str -> Stream.of(str.split("&")))
                    .toList();
        } catch (IOException e) {
            throw new ExchangeRatesServletOperationException("Cannot get reader for request: " + req, e);
        }
    }

    private boolean validateParameters(List<String> params) {
        List<String> requiredParameters = Arrays.asList("baseCurrencyCode", "targetCurrencyCode", "rate");
        if (params.size() != 3) {
            return false;
        }
        List<String> enteredParams = params.stream()
                .flatMap(keyValue -> Stream.of(keyValue.split("=")))
                .toList();
        return enteredParams.containsAll(requiredParameters);
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
