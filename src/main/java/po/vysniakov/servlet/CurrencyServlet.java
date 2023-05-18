package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.model.Currency;
import po.vysniakov.model.Message;
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.CurrencyRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet(urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        CrudRepository<Currency> crudRepository = new CurrencyRepository();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Optional<String> currency = getRequestedCurrency(req);

        if (currency.isEmpty()) {
            String message = new Gson().toJson(new Message("Code cannot be empty ../currency/{currencyCode}"));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        Optional<Currency> foundCurrency = crudRepository.findOne(currency.get());
        if (foundCurrency.isEmpty()) {
            String message = new Gson().toJson(new Message("There is no data about the " + currency.get()));
            setCodeAndJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
            return;
        }

        String json = new Gson().toJson(foundCurrency.get());
        setCodeAndJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    private Optional<String> getRequestedCurrency(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String[] split = pathInfo.split("/");
        if (split.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(split[split.length - 1]);
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
