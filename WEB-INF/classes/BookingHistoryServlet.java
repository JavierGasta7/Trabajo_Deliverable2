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

        BookingData.completeExpiredBookings(connection);

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();

        Vector<BookingData> history = BookingData.getHistory(connection, userId);
        out.print("[");
        for (int i = 0; i < history.size(); i++) {
            BookingData b = history.elementAt(i);
            if (i > 0) out.print(",");
            out.print("{");
            out.print("\"bookingId\":" + b.bookingId + ",");
            out.print("\"classId\":" + b.classId + ",");
            out.print("\"className\":\"" + b.className + "\",");
            out.print("\"activityType\":\"" + b.activityType + "\",");
            out.print("\"instructorName\":\"" + b.instructorName + "\",");
            out.print("\"classDate\":\"" + Utils.formatDate(b.classDate) + "\",");
            out.print("\"startTime\":\"" + Utils.formatTime(b.startTime) + "\",");
            out.print("\"status\":\"" + b.status + "\"");
            out.print("}");
        }
        out.print("]");
        out.close();
    }
}