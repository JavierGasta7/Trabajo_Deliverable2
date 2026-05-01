import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class MaintenanceAlertServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;
        if (role == null || !role.equalsIgnoreCase("admin")) {
            res.sendRedirect("index.html");
            return;
        }

        Vector<EquipmentData> all = EquipmentData.getAll(connection, null, null);

        Vector<EquipmentData> critical = new Vector<EquipmentData>();
        Vector<EquipmentData> high     = new Vector<EquipmentData>();
        Vector<EquipmentData> medium   = new Vector<EquipmentData>();

        long now = System.currentTimeMillis();
        long sixMonthsMs = 1000L * 60 * 60 * 24 * 30 * 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < all.size(); i++) {
            EquipmentData e = all.elementAt(i);
            if ("broken".equals(e.status)) {
                critical.addElement(e);
                continue;
            }
            boolean oldMaintenance = false;
            if (e.lastMaintenance != null && e.lastMaintenance.length() >= 10) {
                try {
                    Date d = sdf.parse(e.lastMaintenance.substring(0, 10));
                    if (now - d.getTime() > sixMonthsMs) oldMaintenance = true;
                } catch (Exception ex) { /* ignore parse */ }
            } else {
                oldMaintenance = true;
            }
            if (oldMaintenance) {
                high.addElement(e);
            } else if ("maintenance".equals(e.status)) {
                medium.addElement(e);
            }
        }

        if ("json".equals(req.getParameter("format"))) {
            res.setContentType("application/json; charset=UTF-8");
            PrintWriter jout = res.getWriter();
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"counts\":{\"critical\":").append(critical.size())
              .append(",\"high\":").append(high.size())
              .append(",\"medium\":").append(medium.size()).append("},");
            sb.append("\"critical\":").append(toJsonArray(critical)).append(",");
            sb.append("\"high\":").append(toJsonArray(high)).append(",");
            sb.append("\"medium\":").append(toJsonArray(medium));
            sb.append("}");
            jout.print(sb.toString());
            jout.close();
            return;
        }

        PrintWriter out = res.getWriter();
        out.println(Utils.header("Alertas de mantenimiento", session));
        out.println("<div class='card' style='max-width:1000px; margin:20px auto;'>");
        out.println("<div class='title'>Alertas de mantenimiento</div>");
        out.println("<div class='subtitle'>Equipos que requieren atenci\u00f3n, priorizados por criticidad</div>");

        out.println("<div style='display:flex; gap:15px; justify-content:center; margin:20px 0;'>");
        out.println(summaryBadge("Cr\u00edticos", critical.size(), "#ef4444"));
        out.println(summaryBadge("Altos",     high.size(),     "#f59e0b"));
        out.println(summaryBadge("Medios",    medium.size(),   "#eab308"));
        out.println("</div>");

        renderSection(out, "Cr\u00edticos (equipos rotos)", critical, "#ef4444", "#fef2f2");
        renderSection(out, "Altos (>6 meses sin revisi\u00f3n)", high, "#f59e0b", "#fffbeb");
        renderSection(out, "Medios (en mantenimiento)", medium, "#eab308", "#fefce8");

        if (critical.size() == 0 && high.size() == 0 && medium.size() == 0) {
            out.println("<div class='success' style='text-align:center;'>Todo el equipamiento est\u00e1 en buen estado. \u00a1Sin alertas!</div>");
        }

        out.println("<div style='text-align:center; margin-top:20px;'>");
        out.println("<a href='EquipmentListServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver al listado</button></a>");
        out.println("</div>");

        out.println("</div>");
        out.println(Utils.footer("Maintenance Alerts"));
        out.close();
    }

    private String summaryBadge(String label, int count, String color) {
        return "<div style='flex:1; max-width:200px; text-align:center; padding:15px; border-radius:8px; " +
               "background:" + color + "; color:white;'>" +
               "<div style='font-size:28px; font-weight:bold;'>" + count + "</div>" +
               "<div style='font-size:13px;'>" + label + "</div>" +
               "</div>";
    }

    private void renderSection(PrintWriter out, String title, Vector<EquipmentData> list, String borderColor, String bgColor) {
        if (list.size() == 0) return;
        out.println("<h3 style='color:" + borderColor + "; margin-top:25px;'>" + title + " (" + list.size() + ")</h3>");
        for (int i = 0; i < list.size(); i++) {
            EquipmentData e = list.elementAt(i);
            out.println("<div style='border-left:4px solid " + borderColor + "; background:" + bgColor +
                        "; padding:12px 16px; margin-bottom:10px; border-radius:4px; display:flex; justify-content:space-between; align-items:center;'>");
            out.println("<div>");
            out.println("<strong>" + safe(e.name) + "</strong> " +
                        "<span style='color:#6b7280; font-size:13px;'>(" + safe(e.type) + " \u00b7 " + safe(e.room) + ")</span><br>");
            out.println("<span style='font-size:12px; color:#6b7280;'>Estado: " + safe(e.status) +
                        " \u00b7 \u00daltima revisi\u00f3n: " + safeDate(e.lastMaintenance) + "</span>");
            out.println("</div>");
            out.println("<a href='EditEquipmentServlet?id=" + e.equipmentId + "'><button class='primary' type='button' style='width:auto; padding:6px 14px;'>Resolver</button></a>");
            out.println("</div>");
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String safeDate(String s) {
        if (s == null || s.length() < 10) return "nunca";
        return s.substring(0, 10);
    }

    private String jsonEsc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private String toJsonArray(Vector<EquipmentData> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            EquipmentData e = list.elementAt(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"equipmentId\":").append(e.equipmentId).append(",");
            sb.append("\"name\":\"").append(jsonEsc(e.name)).append("\",");
            sb.append("\"type\":\"").append(jsonEsc(e.type)).append("\",");
            sb.append("\"room\":\"").append(jsonEsc(e.room)).append("\",");
            sb.append("\"status\":\"").append(jsonEsc(e.status)).append("\",");
            sb.append("\"lastMaintenance\":\"").append(jsonEsc(safeDate(e.lastMaintenance))).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
