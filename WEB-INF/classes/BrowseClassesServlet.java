import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class BrowseClassesServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        String userName = (String) session.getAttribute("userName");
        String userRole = (String) session.getAttribute("userRole");

        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter toClient = res.getWriter();
        toClient.println(Utils.header("Browse Classes", session));

        toClient.println("<p style='text-align:center; color:#6b7280;'>" +
                         "Logged in as <b>" + userName + "</b> (" + userRole + ")</p>");

        // Lectura de los filtros
        String filterActivity = req.getParameter("activity");
        String filterInstructor = req.getParameter("instructor");

        // Formulario de filtros
        toClient.println("<form action='BrowseClassesServlet' method='GET' style='text-align:center; margin:20px;'>");
        toClient.println("Activity: <input type='text' name='activity' value='" +
                         (filterActivity == null ? "" : filterActivity) +
                         "' placeholder='Yoga, Spinning, ...' /> ");
        toClient.println("Instructor id: <input type='text' name='instructor' value='" +
                         (filterInstructor == null ? "" : filterInstructor) +
                         "' placeholder='2, 6, ...' /> ");
        toClient.println("<input type='submit' value='Apply Filters' style='width:auto; padding:8px 16px;' />");
        toClient.println("</form>");

        // Lista de clases
        Vector<ClassData> classList = ClassData.getAvailableClasses(connection, filterActivity, filterInstructor);

        toClient.println("<table>");
        toClient.println("<tr><th>Class</th><th>Activity</th><th>Instructor</th><th>Room</th>" +
                         "<th>Date</th><th>Time</th><th>Spots</th><th>Action</th></tr>");
        for (int i = 0; i < classList.size(); i++) {
            ClassData c = classList.elementAt(i);
            String spots = c.bookedCount + "/" + c.maxCapacity;
            String action;
            if (c.bookedCount >= c.maxCapacity) {
                action = "<span style='color:#ef4444;'>Full</span>";
            } else {
                action = "<a href='BookClassServlet?classId=" + c.classId + "'>Book &rarr;</a>";
            }
            toClient.println("<tr>");
            toClient.println("<td>" + c.name + "</td>");
            toClient.println("<td>" + c.activityType + "</td>");
            toClient.println("<td>" + c.instructorName + "</td>");
            toClient.println("<td>" + c.room + "</td>");
            toClient.println("<td>" + c.classDate + "</td>");
            toClient.println("<td>" + c.startTime + "</td>");
            toClient.println("<td>" + spots + "</td>");
            toClient.println("<td>" + action + "</td>");
            toClient.println("</tr>");
        }
        toClient.println("</table>");

        toClient.println(Utils.footer("Browse Classes"));
        toClient.close();
    }
}
