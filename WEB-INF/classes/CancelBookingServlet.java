import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class CancelBookingServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        res.setContentType("text/html");
        PrintWriter toClient = res.getWriter();
        toClient.println(Utils.header("Cancel Booking"));

        String bookingIdStr = req.getParameter("bookingId");
        if (bookingIdStr == null) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Missing bookingId parameter.</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Cancel Booking"));
            toClient.close();
            return;
        }
        int bookingId = Integer.parseInt(bookingIdStr);

        // Validacion 1: la reserva existe y es del usuario logueado
        BookingData b = BookingData.getBooking(connection, bookingId);
        if (b == null) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Booking not found (id=" + bookingId + ")</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Cancel Booking"));
            toClient.close();
            return;
        }
        if (b.memberId != userId) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>You can only cancel your own bookings.</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Cancel Booking"));
            toClient.close();
            return;
        }

        // Validacion 2: el booking no esta ya cancelado
        if ("cancelled".equals(b.status)) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>This booking is already cancelled.</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Cancel Booking"));
            toClient.close();
            return;
        }

        // Validacion 3: faltan mas de 2 horas para el inicio de la clase
        try {
            // b.classDate viene como "yyyy-MM-dd HH:mm:ss" o similar
            // b.startTime viene como "yyyy-MM-dd HH:mm:ss" donde la fecha es 1899-12-30
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String dateOnly = b.classDate.substring(0, 10);
            String timeOnly = b.startTime.substring(11, 19);
            Date classDateTime = sdfFull.parse(dateOnly + " " + timeOnly);
            Date now = new Date();
            long diffMillis = classDateTime.getTime() - now.getTime();
            long diffHours = diffMillis / (1000 * 60 * 60);

            if (diffHours < 2) {
                toClient.println("<div class='card'>");
                toClient.println("<div class='error'>Cancel deadline passed. You must cancel at least 2 hours before the class starts.</div>");
                toClient.println("<p>Class: <b>" + b.className + "</b> on " + dateOnly + " at " + timeOnly + "</p>");
                toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
                toClient.println("</div>");
                toClient.println(Utils.footer("Cancel Booking"));
                toClient.close();
                return;
            }
        } catch (Exception ex) {
            System.out.println("Error parsing dates: " + ex);
            // si no podemos parsear, dejamos que cancele igualmente
        }

        // Todo OK, cancelamos
        int n = BookingData.cancelBooking(connection, bookingId);
        if (n > 0) {
            toClient.println("<div class='card'>");
            toClient.println("<div class='success'>Booking cancelled. The spot has been released.</div>");
            toClient.println("<p><b>Class:</b> " + b.className + "</p>");
            toClient.println("<p><b>Date:</b> " + b.classDate.substring(0, 10) + " " + b.startTime.substring(11, 19) + "</p>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
        } else {
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Could not cancel the booking. Try again later.</div>");
            toClient.println("<a href='BookingHistoryServlet'><button class='primary' type='button'>Back to my bookings</button></a>");
            toClient.println("</div>");
        }

        toClient.println(Utils.footer("Cancel Booking"));
        toClient.close();
    }
}
