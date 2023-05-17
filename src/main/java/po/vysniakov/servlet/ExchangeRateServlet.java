package po.vysniakov.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import po.vysniakov.model.ExchangeRate;
import po.vysniakov.repositories.CrudRepository;
import po.vysniakov.repositories.ExchangeRateRepository;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        CrudRepository<ExchangeRate> repository = new ExchangeRateRepository();
        getRequestedCurrencyPair(req);
    }

    private void getRequestedCurrencyPair(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String[] split = pathInfo.split("/");
        //Currency CODE length = 3


    }
}
