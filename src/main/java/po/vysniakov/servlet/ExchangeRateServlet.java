package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.model.ErrorMessage;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.ExchangeRateRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Optional<String> requestedCurrencyPair = getRequestedCurrencyPair(req);
        if (requestedCurrencyPair.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        CrudRepository<ExchangeRate> repository = new ExchangeRateRepository();
        Optional<ExchangeRate> rate = repository.findOne(requestedCurrencyPair.get());
        if (rate.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = resp.getWriter();
            writer.write(new Gson().toJson(new ErrorMessage("The exchange rate has not been found")));
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
