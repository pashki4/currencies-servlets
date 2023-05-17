package po.vysniakov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.model.Currency;
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
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Code cannot be empty ../currency/{currencyCode}");
            return;
        }

        Optional<Currency> foundCurrency = crudRepository.findOne(currency.get());
        foundCurrency.ifPresentOrElse(c -> {
                    String currencyJson = new Gson().toJson(c);
                    writeJsonToResponse(resp, currencyJson);
                }, () -> sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                        "There is no data about the " + currency.get())
        );
    }

    private static void writeJsonToResponse(HttpServletResponse resp, String json) {
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(json);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot get print writer", e);
        }
    }

    private Optional<String> getRequestedCurrency(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String[] split = pathInfo.split("/");
        if (split.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(split[split.length - 1]);
    }

    private static void sendError(HttpServletResponse resp, int code, String message) {
        try {
            resp.sendError(code, message);
        } catch (IOException e) {
            throw new RuntimeException("Exception while sending error through response", e);
        }
    }
}
