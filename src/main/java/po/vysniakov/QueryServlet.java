package po.vysniakov;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(urlPatterns = "/query")
public class QueryServlet extends HttpServlet {

    private static final String URL = "jdbc:sqlite:D:/Dev/sqlite/db/mydb";
    private static final String SELECT_SQL = "SELECT * FROM books WHERE author = ?";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        // Get an output writer to write the response message into the network socket
        PrintWriter out = response.getWriter();

        // Print an HTML page as the output of the query
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Query Response</title></head>");
        out.println("<body>");

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedSelectStatement = connection.prepareStatement(SELECT_SQL)) {
            preparedSelectStatement.setString(1, request.getParameter("author"));
            ResultSet resultSet = preparedSelectStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                // Print a paragraph <p>...</p> for each record
                out.println("<p>" + resultSet.getString("author")
                        + ", " + resultSet.getString("title")
                        + ", $" + resultSet.getDouble("price") + "</p>");
                count++;
            }
            out.println("<p>==== " + count + " records found =====</p>");
            out.println("</body></html>");
        } catch (SQLException e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("<p>Check Tomcat console for details.</p>");
        } finally {
            out.close();
        }
    }
}
