package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.exception.ExchangeRateServletOperationException;
import po.vysniakov.model.ErrorMessage;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.ExchangeRateRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Optional<String> requestedCurrencyPair = getRequestedCurrencyPair(req);
        if (requestedCurrencyPair.isEmpty() || requestedCurrencyPair.get().length() != 6) {
            String error = new Gson().toJson(new ErrorMessage("Bad request"));
            setCodeAndWriteJsonToResponse(resp, HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }

        CrudRepository<ExchangeRate> repository = new ExchangeRateRepository();
        Optional<ExchangeRate> rate = repository.findOne(requestedCurrencyPair.get());
        if (rate.isEmpty()) {
            String error = new Gson().toJson(new ErrorMessage("The exchange rate was not found"));
            setCodeAndWriteJsonToResponse(resp, HttpServletResponse.SC_NOT_FOUND, error);
            return;
        }

        String json = new Gson().toJson(rate.get());
        setCodeAndWriteJsonToResponse(resp, HttpServletResponse.SC_OK, json);
    }

    private void setCodeAndWriteJsonToResponse(HttpServletResponse resp, int code, String json) {
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
}
