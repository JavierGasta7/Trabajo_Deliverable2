import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class BookingHistoryServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int    userId   = (int)    session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        res.setContentType("text/html");
        PrintWriter toClient = res.getWriter();
        toClient.println(Utils.header("My Bookings"));

        toClient.println("<p style='text-align:center; color:#6b7280;'>" +
                         "Bookings of <b>" + userName + "</b></p>");

        Vector<BookingData> history = BookingData.getHistory(connection, userId);

        if (history.size() == 0) {
            toClient.println("<div class='card'>");
            toClient.println("<p style='text-align:center;'>You have no bookings yet.</p>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Browse classes</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("My Bookings"));
            toClient.close();
            return;
        }

        toClient.println("<table>");
        toClient.println("<tr><th>Class</th><th>Activity</th><th>Instructor</th>" +
                         "<th>Date</th><th>Time</th><th>Status</th><th>Action</th></tr>");
        for (int i = 0; i < history.size(); i++) {
            BookingData b = history.elementAt(i);
            String date = b.classDate.length() >= 10 ? b.classDate.substring(0, 10) : b.classDate;
            String time = b.startTime.length() >= 19 ? b.startTime.substring(11, 19) : b.startTime;

            String action;
            if ("confirmed".equals(b.status)) {
                action = "<a href='CancelBookingServlet?bookingId=" + b.bookingId + "'>Cancel</a>";
            } else if ("completed".equals(b.status)) {
                // El servlet RateClassServlet lo hace Ander (FR7), enlazamos por convencion
                action = "<a href='RateClassServlet?classId=" + b.classId + "'>Rate &#9733;</a>";
            } else {
                action = "&mdash;";
            }

            String color;
            if ("confirmed".equals(b.status)) color = "#2563eb";
            else if ("completed".equals(b.status)) color = "#10b981";
            else color = "#9ca3af";

            toClient.println("<tr>");
            toClient.println("<td>" + b.className + "</td>");
            toClient.println("<td>" + b.activityType + "</td>");
            toClient.println("<td>" + b.instructorName + "</td>");
            toClient.println("<td>" + date + "</td>");
            toClient.println("<td>" + time + "</td>");
            toClient.println("<td style='color:" + color + "; font-weight:bold;'>" + b.status + "</td>");
            toClient.println("<td>" + action + "</td>");
            toClient.println("</tr>");
        }
        toClient.println("</table>");

        toClient.println("<div style='text-align:center; margin:20px;'>");
        toClient.println("<a href='BrowseClassesServlet'>Browse more classes &rarr;</a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("My Bookings"));
        toClient.close();
    }
}
