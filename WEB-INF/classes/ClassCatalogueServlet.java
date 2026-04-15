import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ClassCatalogueServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String userRole = (String) session.getAttribute("userRole");

        if (!"admin".equalsIgnoreCase(userRole)) {
            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;

        out.println(Utils.header("Manage Classes"));

        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            // ================= EDIT FORM =================
            if ("edit".equals(action) && idParam != null) {

                int id = Integer.parseInt(idParam);

                String sql = "SELECT * FROM classes WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    out.println("<div class='card' style='max-width:600px;'>");
                    out.println("<form method='post' action='ClassCatalogueServlet'>");

                    out.println("<input type='hidden' name='action' value='edit'>");
                    out.println("<input type='hidden' name='id' value='" + id + "'>");

                    out.println("<label>Name</label><br>");
                    out.println("<input type='text' name='name' value='" + rs.getString("name") + "'><br><br>");

                    out.println("<button class='primary'>Save</button>");

                    out.println("</form>");
                    out.println("</div>");
                }

                rs.close();
                ps.close();
            }

            // ================= LIST =================
            String sql = "SELECT * FROM classes ORDER BY class_date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.println("<div class='card'>");

            while (rs.next()) {

                int id = rs.getInt("id");

                out.println("<div style='border:1px solid #ddd; padding:15px; margin-bottom:15px;'>");

                out.println("<b>" + rs.getString("name") + "</b><br>");
                out.println("Date: " + rs.getString("class_date") + "<br>");
                out.println("Time: " + rs.getString("start_time") + "<br>");
                out.println("Room: " + rs.getString("room") + "<br><br>");

                // EDIT
                out.println("<a href='ClassCatalogueServlet?action=edit&id=" + id + "'>");
                out.println("<button class='secondary'>Edit</button></a> ");

                // CANCEL (POST)
                out.println("<form method='post' action='ClassCatalogueServlet' style='display:inline;'>");
                out.println("<input type='hidden' name='action' value='cancel'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<button style='background:#dc3545;color:white;' ");
                out.println("onclick=\"return confirm('Cancel this class?')\">Cancel</button>");
                out.println("</form>");

                out.println("</div>");
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

            int id = Integer.parseInt(idParam);

            // CANCEL
            if ("cancel".equals(action)) {

                String sql = "UPDATE bookings SET status='cancelled' WHERE class_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // EDIT
            if ("edit".equals(action)) {

                String name = request.getParameter("name");

                String sql = "UPDATE classes SET name=? WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(conn);
        }

        response.sendRedirect("ClassCatalogueServlet");
    }
}