import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null ||
            !"admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            response.sendRedirect("index.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;

        out.println(Utils.header("Admin Dashboard, Administration Panel"));


        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            // ================= METRICS =================
            int members = getCount(conn, "SELECT COUNT(*) FROM users WHERE role='member'");
            int instructors = getCount(conn, "SELECT COUNT(*) FROM users WHERE role='instructor'");
            int classes = getCount(conn, "SELECT COUNT(*) FROM classes");

            out.println("<div class='card' style='display:flex; gap:20px; flex-wrap:wrap;'>");

            out.println(metricBox("Members", members));
            out.println(metricBox("Instructors", instructors));
            out.println(metricBox("Classes", classes));

            out.println("</div>");

            // ================= PENDING USERS =================
            String sql = "SELECT * FROM users WHERE membership_status='pending'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.println("<div class='card'>");
            out.println("<div class='title'>Pending Approvals</div>");

            boolean hasPending = false;

            while (rs.next()) {
                hasPending = true;

                int id = rs.getInt("id");

                out.println("<div style='border:1px solid #ddd; padding:10px; margin-bottom:10px;'>");

                out.println("<b>" + rs.getString("full_name") + "</b><br>");
                out.println(rs.getString("email") + "<br>");

                // APPROVE BUTTON
                out.println("<form method='post' action='AdminDashboardServlet' style='margin-top:8px;'>");
                out.println("<input type='hidden' name='action' value='approve'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<button class='primary'>Approve</button>");
                out.println("</form>");

                out.println("</div>");
            }

            if (!hasPending) {
                out.println("<p>No pending approvals.</p>");
            }

            out.println("</div>");

            rs.close();
            ps.close();

        } catch (Exception e) {
            out.println("<div class='card'><p>Error: " + e.getMessage() + "</p></div>");
        } finally {
            ConnectionUtils.close(conn);
        }

        out.println(Utils.footer(""));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null ||
            !"admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        Connection conn = null;

        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            if ("approve".equals(action)) {

                int id = Integer.parseInt(idParam);

                String sql = "UPDATE users SET membership_status='active' WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(conn);
        }

        response.sendRedirect("AdminDashboardServlet");
    }

    // ================= HELPERS =================

    private int getCount(Connection conn, String sql) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        ps.close();
        return count;
    }

    private String metricBox(String title, int value) {
        return "<div style='flex:1; min-width:150px; background:#f8f9fa; padding:20px; border-radius:10px; text-align:center;'>"
            + "<div style='font-size:1.5rem; font-weight:bold;'>" + value + "</div>"
            + "<div style='color:#6c757d;'>" + title + "</div>"
            + "</div>";
    }
}