import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class BookClassServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int    userId    = (int)    session.getAttribute("userId");
        String userName  = (String) session.getAttribute("userName");
        String userEmail = (String) session.getAttribute("userEmail");

        res.setContentType("text/html");
        PrintWriter toClient = res.getWriter();
        toClient.println(Utils.header("Book Class", session));

        String classIdStr = req.getParameter("classId");
        if (classIdStr == null) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Missing classId parameter.</div>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Back to classes</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Book Class"));
            toClient.close();
            return;
        }
        int classId = Integer.parseInt(classIdStr);

        // Validacion 1: la clase existe
        ClassData c = ClassData.getClass(connection, classId);
        if (c == null) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Class not found (id=" + classId + ")</div>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Back to classes</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Book Class"));
            toClient.close();
            return;
        }

        // Validacion 2: la membresia del usuario esta activa
        UserData me = UserData.getUser(connection, userId);
        if (me == null || !"active".equals(me.membershipStatus)) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Your membership is not active. Please contact the admin.</div>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Back to classes</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Book Class"));
            toClient.close();
            return;
        }

        // Validacion 3: no debe estar ya reservada
        if (BookingData.isAlreadyBooked(connection, userId, classId)) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>You already have a confirmed booking for this class.</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>See my bookings</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Book Class"));
            toClient.close();
            return;
        }

        // Validacion 4: hay plazas libres
        if (c.bookedCount >= c.maxCapacity) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>The class is full (" + c.bookedCount + "/" + c.maxCapacity + ")</div>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Back to classes</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Book Class"));
            toClient.close();
            return;
        }

        // Todo OK, hacemos la reserva
        int n = BookingData.insertBooking(connection, userId, classId);
        if (n > 0) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='success'>Booking confirmed!</div>");
            toClient.println("<p><b>Member:</b> " + userName + " (" + userEmail + ")</p>");
            toClient.println("<p><b>Class:</b> " + c.name + " (" + c.activityType + ")</p>");
            toClient.println("<p><b>Instructor:</b> " + c.instructorName + "</p>");
            toClient.println("<p><b>Date / Time:</b> " + c.classDate + " " + c.startTime + "</p>");
            toClient.println("<p><b>Room:</b> " + c.room + "</p>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>See my bookings</button></a>");
            toClient.println("<a href='BrowseClassesServlet'><button class='secondary' type='button'>Browse more classes</button></a>");
            toClient.println("</div>");
        } else {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Could not save the booking. Try again later.</div>");
            toClient.println("<a href='BrowseClassesServlet'><button class='primary' type='button'>Back to classes</button></a>");
            toClient.println("</div>");
        }

        toClient.println(Utils.footer("Book Class"));
        toClient.close();
    }
}
