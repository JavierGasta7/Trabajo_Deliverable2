import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class OccupancyMonitorServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int    userId   = (int)    session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        String userName = (String) session.getAttribute("userName");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;

        out.println(Utils.header("Occupancy Monitor"));


        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            String sql =
                "SELECT c.id, c.name, c.activity_type, c.room, c.class_date, " +
                "       c.start_time, c.duration_min, c.max_capacity, " +
                "       COUNT(b.id) AS booked_count " +
                "FROM classes c " +
                "LEFT JOIN bookings b ON c.id = b.class_id " +
                "    AND b.status = 'confirmed' " +
                "GROUP BY c.id, c.name, c.activity_type, c.room, c.class_date, " +
                "         c.start_time, c.duration_min, c.max_capacity " +
                "ORDER BY c.class_date DESC, c.start_time ASC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.println("<div class='card' style='max-width:900px; margin:32px auto;'>");
            out.println("  <div class='title'>Live Occupancy</div>");
            out.println("  <div class='subtitle'>All scheduled classes and their current booking status</div>");
            out.println("  <div id='occupancy-list' style='margin-top:24px;'>");

            boolean hasRows = false;

            while (rs.next()) {
                hasRows = true;

                int    id          = rs.getInt("id");
                String name        = rs.getString("name");
                String actType     = rs.getString("activity_type");
                String room        = rs.getString("room");
                String classDate   = rs.getString("class_date");
                String startTime   = rs.getString("start_time");
                int    duration    = rs.getInt("duration_min");
                int    maxCap      = rs.getInt("max_capacity");
                int    booked      = rs.getInt("booked_count");

                int    available   = maxCap - booked;
                double pct         = (maxCap > 0) ? (booked * 100.0 / maxCap) : 0;
                boolean isFull     = (booked >= maxCap);

                String rowStyle = isFull
                    ? "border:1.5px solid #dc3545; background:#fff5f5; border-radius:10px; padding:18px 20px; margin-bottom:16px;"
                    : "border:1px solid #dee2e6; border-radius:10px; padding:18px 20px; margin-bottom:16px;";

                out.println("<div style='" + rowStyle + "'>");

                out.println("  <div style='display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;'>");
                out.println("    <div>");
                out.println("      <strong style='font-size:1.05rem;'>" + escapeHtml(name) + "</strong>");
                if (isFull) {
                    out.println("      <span style='margin-left:10px; background:#dc3545; color:#fff; font-size:0.75rem; padding:2px 9px; border-radius:20px; font-weight:600; letter-spacing:.4px;'>FULL</span>");
                }
                out.println("    </div>");
                out.println("    <div style='font-size:0.85rem; color:#6c757d;'>");
                out.println("      " + escapeHtml(classDate) + " &nbsp;|&nbsp; " + escapeHtml(startTime) + " &nbsp;|&nbsp; " + duration + " min");
                out.println("    </div>");
                out.println("  </div>");

                out.println("  <div style='margin:8px 0 12px; font-size:0.82rem; color:#6c757d;'>");
                out.println("    <span style='background:#f1f3f5; border-radius:4px; padding:2px 8px; margin-right:6px;'>" + escapeHtml(actType) + "</span>");
                out.println("    <span style='background:#f1f3f5; border-radius:4px; padding:2px 8px;'>&#x1F4CD; " + escapeHtml(room) + "</span>");
                out.println("  </div>");

                String barColor = isFull ? "#dc3545" : (pct >= 80 ? "#fd7e14" : "#28a745");
                out.println("  <div style='display:flex; align-items:center; gap:12px;'>");
                out.println("    <div style='flex:1; background:#e9ecef; border-radius:6px; height:14px; overflow:hidden;'>");
                out.println("      <div style='width:" + (int)pct + "%; background:" + barColor + "; height:100%; border-radius:6px; transition:width .4s;'></div>");
                out.println("    </div>");
                out.println("    <span style='font-size:0.88rem; font-weight:600; color:" + barColor + "; min-width:40px; text-align:right;'>" + (int)pct + "%</span>");
                out.println("    <span style='font-size:0.82rem; color:#6c757d; min-width:90px; text-align:right;'>");
                out.println("      " + booked + " / " + maxCap + " &nbsp;(" + available + " free)");
                out.println("    </span>");
                out.println("  </div>");

                out.println("</div>");
            }

            if (!hasRows) {
                out.println("<p style='color:#6c757d; text-align:center; padding:24px 0;'>No classes found in the database.</p>");
            }

            out.println("  </div>");

            out.println("  <div style='margin-top:20px; padding-top:16px; border-top:1px solid #dee2e6; display:flex; gap:20px; font-size:0.82rem; color:#6c757d; flex-wrap:wrap;'>");
            out.println("    <span><span style='display:inline-block;width:12px;height:12px;background:#28a745;border-radius:2px;margin-right:5px;'></span>Available (&lt;80%)</span>");
            out.println("    <span><span style='display:inline-block;width:12px;height:12px;background:#fd7e14;border-radius:2px;margin-right:5px;'></span>Nearly full (80–99%)</span>");
            out.println("    <span><span style='display:inline-block;width:12px;height:12px;background:#dc3545;border-radius:2px;margin-right:5px;'></span>Full (100%)</span>");
            out.println("  </div>");

            out.println("</div>");

            rs.close();
            ps.close();

        } catch (SQLException e) {
            out.println("<div class='card' style='max-width:900px; margin:32px auto; border:1px solid #dc3545;'>");
            out.println("  <p style='color:#dc3545;'><strong>Database error:</strong> " + escapeHtml(e.getMessage()) + "</p>");
            out.println("</div>");
        } finally {
            ConnectionUtils.close(conn);
        }

        out.println(Utils.footer(""));
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}