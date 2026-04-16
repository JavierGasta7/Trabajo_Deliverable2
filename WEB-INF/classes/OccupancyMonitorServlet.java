import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class OccupancyMonitorServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("index.html");
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        out.println(Utils.header("Monitor de Ocupaci\u00f3n", session));

        try {
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

            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int totalClasses = 0, fullClasses = 0, totalBooked = 0, totalCapacity = 0;

            StringBuilder rows = new StringBuilder();
            while (rs.next()) {
                totalClasses++;
                String name      = rs.getString("name");
                String actType   = rs.getString("activity_type");
                String room      = rs.getString("room");
                String classDate = Utils.formatDate(rs.getString("class_date"));
                String startTime = Utils.formatTime(rs.getString("start_time"));
                int duration     = rs.getInt("duration_min");
                int maxCap       = rs.getInt("max_capacity");
                int booked       = rs.getInt("booked_count");

                totalBooked += booked;
                totalCapacity += maxCap;

                int available  = maxCap - booked;
                double pct     = (maxCap > 0) ? (booked * 100.0 / maxCap) : 0;
                boolean isFull = (booked >= maxCap);
                if (isFull) fullClasses++;

                String rowStyle = isFull
                    ? "border:1.5px solid #dc3545; background:#fff5f5; border-radius:10px; padding:18px 20px; margin-bottom:16px;"
                    : "border:1px solid #dee2e6; border-radius:10px; padding:18px 20px; margin-bottom:16px;";

                rows.append("<div style='").append(rowStyle).append("'>");
                rows.append("<div style='display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;'>");
                rows.append("<div><strong style='font-size:1.05rem;'>").append(escapeHtml(name)).append("</strong>");
                if (isFull) {
                    rows.append("<span style='margin-left:10px; background:#dc3545; color:#fff; font-size:0.75rem; padding:2px 9px; border-radius:20px; font-weight:600;'>COMPLETO</span>");
                }
                rows.append("</div>");
                rows.append("<div style='font-size:0.85rem; color:#6c757d;'>")
                    .append(escapeHtml(classDate)).append(" &nbsp;|&nbsp; ")
                    .append(escapeHtml(startTime)).append(" &nbsp;|&nbsp; ")
                    .append(duration).append(" min</div>");
                rows.append("</div>");

                rows.append("<div style='margin:8px 0 12px; font-size:0.82rem; color:#6c757d;'>");
                rows.append("<span style='background:#f1f3f5; border-radius:4px; padding:2px 8px; margin-right:6px;'>")
                    .append(escapeHtml(actType)).append("</span>");
                rows.append("<span style='background:#f1f3f5; border-radius:4px; padding:2px 8px;'>Sala ")
                    .append(escapeHtml(room)).append("</span>");
                rows.append("</div>");

                String barColor = isFull ? "#dc3545" : (pct >= 80 ? "#fd7e14" : "#28a745");
                rows.append("<div style='display:flex; align-items:center; gap:12px;'>");
                rows.append("<div style='flex:1; background:#e9ecef; border-radius:6px; height:14px; overflow:hidden;'>");
                rows.append("<div style='width:").append((int) pct).append("%; background:").append(barColor)
                    .append("; height:100%; border-radius:6px;'></div></div>");
                rows.append("<span style='font-size:0.88rem; font-weight:600; color:").append(barColor)
                    .append("; min-width:40px; text-align:right;'>").append((int) pct).append("%</span>");
                rows.append("<span style='font-size:0.82rem; color:#6c757d; min-width:120px; text-align:right;'>")
                    .append(booked).append(" / ").append(maxCap).append(" (").append(available).append(" libres)</span>");
                rows.append("</div>");
                rows.append("</div>");
            }
            rs.close();
            ps.close();

            int globalPct = (totalCapacity > 0) ? (int)(totalBooked * 100.0 / totalCapacity) : 0;

            out.println("<div class='card' style='max-width:900px; margin:20px auto;'>");
            out.println("<div class='title'>Ocupaci\u00f3n en vivo</div>");
            out.println("<div class='subtitle'>Estado actual de todas las clases programadas</div>");

            out.println("<div style='display:flex; gap:12px; justify-content:center; margin:20px 0; flex-wrap:wrap;'>");
            out.println(chip("Clases", totalClasses, "#2563eb"));
            out.println(chip("Completas", fullClasses, "#dc3545"));
            out.println(chip("Reservas", totalBooked, "#10b981"));
            out.println(chip("Ocupaci\u00f3n global", globalPct + "%", "#7c3aed"));
            out.println("</div>");

            out.println("<div style='margin-top:16px;'>");
            if (totalClasses == 0) {
                out.println("<p style='color:#6c757d; text-align:center; padding:24px 0;'>No hay clases registradas.</p>");
            } else {
                out.println(rows.toString());
            }
            out.println("</div>");

            out.println("<div style='margin-top:20px; padding-top:16px; border-top:1px solid #dee2e6; display:flex; gap:20px; font-size:0.82rem; color:#6c757d; flex-wrap:wrap;'>");
            out.println("<span><span style='display:inline-block;width:12px;height:12px;background:#28a745;border-radius:2px;margin-right:5px;'></span>Disponible (&lt;80%)</span>");
            out.println("<span><span style='display:inline-block;width:12px;height:12px;background:#fd7e14;border-radius:2px;margin-right:5px;'></span>Casi completo (80-99%)</span>");
            out.println("<span><span style='display:inline-block;width:12px;height:12px;background:#dc3545;border-radius:2px;margin-right:5px;'></span>Completo (100%)</span>");
            out.println("</div>");
            out.println("</div>");

        } catch (SQLException e) {
            out.println("<div class='card'><div class='error'>Error de base de datos: " + escapeHtml(e.getMessage()) + "</div></div>");
            e.printStackTrace();
        }

        out.println(Utils.footer("Occupancy"));
        out.close();
    }

    private String chip(String label, Object value, String color) {
        return "<div style='padding:10px 16px; border-radius:8px; background:" + color + "; color:white; min-width:100px; text-align:center;'>" +
               "<div style='font-size:22px; font-weight:bold;'>" + value + "</div>" +
               "<div style='font-size:12px;'>" + label + "</div></div>";
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
