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
import po.vysniakov.exception.ResponseSendErrorException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet(urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DatabaseManager databaseManager = new SQLiteDatabaseManager();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Optional<String> currency = getRequestedCurrency(req);
        if (currency.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Input correct path ../currency/{currencyCode}");
        } else {
            Optional<Currency> current = databaseManager.findOne(currency.get());
            try (PrintWriter writer = resp.getWriter()) {
                current.ifPresentOrElse(x -> writer.println(new Gson().toJson(x)),
                        () -> {
                            try {
                                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                                        "There is no data about " + currency.get());
                            } catch (IOException e) {
                                throw new ResponseSendErrorException("Cannot send error through response", e);
                            }
                        });
            }
        }
    }

    private Optional<String> getRequestedCurrency(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String[] split = pathInfo.split("/");
        return Optional.ofNullable(split[split.length - 1]);
    }
}
