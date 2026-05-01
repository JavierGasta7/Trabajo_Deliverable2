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

        String filterType   = req.getParameter("type");
        String filterStatus = req.getParameter("status");

        if ("json".equals(req.getParameter("format"))) {
            res.setContentType("application/json; charset=UTF-8");
            PrintWriter jout = res.getWriter();
            Vector<EquipmentData> jl = EquipmentData.getAll(connection, filterType, filterStatus);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < jl.size(); i++) {
                EquipmentData e = jl.elementAt(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"equipmentId\":").append(e.equipmentId).append(",");
                sb.append("\"name\":\"").append(jsonEsc(e.name)).append("\",");
                sb.append("\"type\":\"").append(jsonEsc(e.type)).append("\",");
                sb.append("\"room\":\"").append(jsonEsc(e.room)).append("\",");
                sb.append("\"status\":\"").append(jsonEsc(e.status)).append("\",");
                sb.append("\"purchasedAt\":\"").append(jsonEsc(safeDate(e.purchasedAt))).append("\",");
                sb.append("\"lastMaintenance\":\"").append(jsonEsc(safeDate(e.lastMaintenance))).append("\",");
                sb.append("\"notes\":\"").append(jsonEsc(e.notes)).append("\"");
                sb.append("}");
            }
            sb.append("]");
            jout.print(sb.toString());
            jout.close();
            return;
        }

        PrintWriter out = res.getWriter();
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

        int total = list.size();
        int avail = 0, maint = 0, broken = 0;
        for (int i = 0; i < total; i++) {
            String st = list.elementAt(i).status;
            if ("available".equals(st)) avail++;
            else if ("maintenance".equals(st)) maint++;
            else if ("broken".equals(st)) broken++;
        }
        out.println("<div style='display:flex; gap:10px; justify-content:center; margin:10px 0; flex-wrap:wrap;'>");
        out.println("<div style='padding:8px 14px; border-radius:8px; background:#6b7280; color:white;'><b>" + total + "</b> total</div>");
        out.println("<div style='padding:8px 14px; border-radius:8px; background:#10b981; color:white;'><b>" + avail + "</b> disponibles</div>");
        out.println("<div style='padding:8px 14px; border-radius:8px; background:#f59e0b; color:white;'><b>" + maint + "</b> mantenimiento</div>");
        out.println("<div style='padding:8px 14px; border-radius:8px; background:#ef4444; color:white;'><b>" + broken + "</b> rotos</div>");
        out.println("</div>");

        out.println("<div style='text-align:center; margin:10px 0;'>");
        out.println("<input type='text' id='busqueda' placeholder='Buscar en la tabla...' oninput='filtrarTabla(\"busqueda\",\"tablaEquipos\")' style='padding:6px; width:60%;'>");
        out.println("</div>");

        out.println("<table id='tablaEquipos'>");
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

    private String jsonEsc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
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
