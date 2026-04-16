import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class EquipmentListServlet extends HttpServlet {
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

        PrintWriter out = res.getWriter();
        String filterType   = req.getParameter("type");
        String filterStatus = req.getParameter("status");

        out.println(Utils.header("Equipamiento", session));

        out.println("<div class='card' style='max-width:1100px; margin:20px auto;'>");
        out.println("<div class='title'>Listado de equipamiento</div>");

        out.println("<form action='EquipmentListServlet' method='GET' style='text-align:center; margin:15px;'>");
        out.println("Tipo: <select name='type' style='padding:6px; margin-right:10px;'>");
        out.println("<option value=''>Todos</option>");
        String[] types = {"cardio", "fuerza", "libre"};
        for (String t : types) {
            String sel = t.equals(filterType) ? " selected" : "";
            out.println("<option value='" + t + "'" + sel + ">" + t + "</option>");
        }
        out.println("</select>");
        out.println("Estado: <select name='status' style='padding:6px; margin-right:10px;'>");
        out.println("<option value=''>Todos</option>");
        String[] statuses = {"available", "in_use", "maintenance", "broken"};
        for (String s : statuses) {
            String sel = s.equals(filterStatus) ? " selected" : "";
            out.println("<option value='" + s + "'" + sel + ">" + s + "</option>");
        }
        out.println("</select>");
        out.println("<input type='submit' value='Filtrar' style='width:auto; padding:6px 14px;'>");
        out.println(" <a href='AddEquipmentServlet' style='margin-left:10px;'><button class='primary' type='button' style='width:auto; padding:6px 14px;'>A\u00f1adir equipo</button></a>");
        out.println("</form>");

        Vector<EquipmentData> list = EquipmentData.getAll(connection, filterType, filterStatus);

        out.println("<table>");
        out.println("<tr><th>Nombre</th><th>Tipo</th><th>Sala</th><th>Estado</th><th>Comprado</th><th>\u00daltima revisi\u00f3n</th><th>Notas</th><th>Acci\u00f3n</th></tr>");
        for (int i = 0; i < list.size(); i++) {
            EquipmentData e = list.elementAt(i);
            out.println("<tr>");
            out.println("<td>" + safe(e.name) + "</td>");
            out.println("<td>" + safe(e.type) + "</td>");
            out.println("<td>" + safe(e.room) + "</td>");
            out.println("<td>" + badge(e.status) + "</td>");
            out.println("<td>" + safeDate(e.purchasedAt) + "</td>");
            out.println("<td>" + safeDate(e.lastMaintenance) + "</td>");
            out.println("<td>" + safe(e.notes) + "</td>");
            out.println("<td><a href='EditEquipmentServlet?id=" + e.equipmentId + "'>Editar</a></td>");
            out.println("</tr>");
        }
        if (list.size() == 0) {
            out.println("<tr><td colspan='8' style='text-align:center; color:#6b7280;'>No hay equipos que coincidan con el filtro.</td></tr>");
        }
        out.println("</table>");

        out.println("<div style='text-align:center; margin-top:20px;'>");
        out.println("<a href='MaintenanceAlertServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Ver alertas de mantenimiento</button></a>");
        out.println("</div>");

        out.println("</div>");
        out.println(Utils.footer("Equipment"));
        out.close();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String safeDate(String s) {
        if (s == null) return "-";
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
    }

    private String badge(String status) {
        if (status == null) return "";
        String color;
        if ("available".equals(status))        color = "#10b981";
        else if ("in_use".equals(status))      color = "#2563eb";
        else if ("maintenance".equals(status)) color = "#f59e0b";
        else if ("broken".equals(status))      color = "#ef4444";
        else                                    color = "#6b7280";
        return "<span style='background:" + color + "; color:white; padding:3px 10px; border-radius:12px; font-size:12px;'>" + status + "</span>";
    }
}
