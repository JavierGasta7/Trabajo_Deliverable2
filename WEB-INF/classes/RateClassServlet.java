import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RateClassServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        String classIdParam = request.getParameter("classId");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println(Utils.header("Rate Class"));


        if (classIdParam == null) {
            out.println("<div class='card'><p>Missing classId.</p></div>");
            out.println(Utils.footer(""));
            return;
        }

        int classId = Integer.parseInt(classIdParam);

        out.println("<div class='card' style='max-width:600px;'>");
        out.println("<form method='post' action='RateClassServlet'>");

        out.println("<input type='hidden' name='classId' value='" + classId + "'>");

        out.println("<label>Stars (1-5)</label><br>");
        out.println("<input type='number' name='stars' min='1' max='5' required><br><br>");

        out.println("<label>Comment (optional)</label><br>");
        out.println("<textarea name='comment' rows='4' style='width:100%;'></textarea><br><br>");

        out.println("<button class='primary' type='submit'>Submit Rating</button>");

        out.println("</form>");
        out.println("</div>");

        out.println(Utils.footer(""));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        String classIdParam = request.getParameter("classId");
        String starsParam   = request.getParameter("stars");
        String comment      = request.getParameter("comment");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;

        out.println(Utils.header("Rate Class Result"));

        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            int classId = Integer.parseInt(classIdParam);
            int stars   = Integer.parseInt(starsParam);

            // VALIDATION: stars range
            if (stars < 1 || stars > 5) {
                out.println("<div class='card'><p>Stars must be between 1 and 5.</p></div>");
                out.println(Utils.footer(""));
                return;
            }

            // VALIDATION: class completed
            String checkCompleted =
                "SELECT COUNT(*) FROM bookings b " +
                "JOIN classes c ON b.class_id = c.id " +
                "WHERE b.member_id=? AND b.class_id=? " +
                "AND b.status='completed'";

            PreparedStatement ps1 = conn.prepareStatement(checkCompleted);
            ps1.setInt(1, userId);
            ps1.setInt(2, classId);

            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int completedCount = rs1.getInt(1);

            if (completedCount == 0) {
                out.println("<div class='card'><p>You can only rate completed classes.</p></div>");
                out.println(Utils.footer(""));
                return;
            }

            // VALIDATION: already rated
            String checkExisting =
                "SELECT COUNT(*) FROM ratings WHERE member_id=? AND class_id=?";

            PreparedStatement ps2 = conn.prepareStatement(checkExisting);
            ps2.setInt(1, userId);
            ps2.setInt(2, classId);

            ResultSet rs2 = ps2.executeQuery();
            rs2.next();

            if (rs2.getInt(1) > 0) {
                out.println("<div class='card'><p>You already rated this class.</p></div>");
                out.println(Utils.footer(""));
                return;
            }

            // INSERT rating
            String insert =
                "INSERT INTO ratings (member_id, class_id, stars, comment, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

            PreparedStatement ps3 = conn.prepareStatement(insert);
            ps3.setInt(1, userId);
            ps3.setInt(2, classId);
            ps3.setInt(3, stars);
            ps3.setString(4, comment);

            ps3.executeUpdate();

            out.println("<div class='card'>");
            out.println("<p style='color:green;'>Rating submitted successfully!</p>");
            out.println("<a href='BookingHistoryServlet'><button class='secondary'>Back</button></a>");
            out.println("</div>");

        } catch (Exception e) {
            out.println("<div class='card'><p style='color:red;'>Error: " + e.getMessage() + "</p></div>");
        } finally {
            ConnectionUtils.close(conn);
        }

        out.println(Utils.footer(""));
    }
}