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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        DatabaseManager databaseManager = new SQLiteDatabaseManager();
        List<Currency> currencies = databaseManager.findAll();
        String json = new Gson().toJson(currencies);
        try (PrintWriter writer = response.getWriter()) {
            writer.println(json);
            writer.flush();
        }
    }
}
