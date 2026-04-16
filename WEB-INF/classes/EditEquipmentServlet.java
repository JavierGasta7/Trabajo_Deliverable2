import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class EditEquipmentServlet extends HttpServlet {
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
        String idStr = req.getParameter("id");
        if (idStr == null) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Falta par\u00e1metro id.</div>");
            out.println("<a href='EquipmentListServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        int id;
        try { id = Integer.parseInt(idStr); }
        catch (NumberFormatException ex) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>id inv\u00e1lido.</div></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        EquipmentData e = EquipmentData.getById(connection, id);
        if (e == null) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Equipo no encontrado (id=" + id + ")</div>");
            out.println("<a href='EquipmentListServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        out.println(Utils.header("Editar equipo", session));
        out.println("<div class='card' style='max-width:560px; margin:30px auto;'>");
        out.println("<div class='title'>Editar equipo #" + e.equipmentId + "</div>");
        out.println("<form method='post' action='EditEquipmentServlet'>");
        out.println("<input type='hidden' name='id' value='" + e.equipmentId + "'>");
        out.println("<label>Nombre</label>");
        out.println("<input type='text' name='name' value='" + attr(e.name) + "' required>");
        out.println("<label>Tipo</label>");
        out.println("<select name='type' required>");
        writeOption(out, "cardio", e.type);
        writeOption(out, "fuerza", e.type);
        writeOption(out, "libre",  e.type);
        out.println("</select>");
        out.println("<label>Sala</label>");
        out.println("<input type='text' name='room' value='" + attr(e.room) + "'>");
        out.println("<label>Estado</label>");
        out.println("<select name='status' required>");
        writeOption(out, "available",   e.status);
        writeOption(out, "in_use",      e.status);
        writeOption(out, "maintenance", e.status);
        writeOption(out, "broken",      e.status);
        out.println("</select>");
        out.println("<label>\u00daltima revisi\u00f3n</label>");
        String lm = (e.lastMaintenance != null && e.lastMaintenance.length() >= 10) ? e.lastMaintenance.substring(0, 10) : "";
        out.println("<input type='date' name='lastMaintenance' value='" + lm + "'>");
        out.println("<label>Notas</label>");
        out.println("<input type='text' name='notes' value='" + attr(e.notes) + "'>");
        out.println("<input type='submit' value='Guardar cambios'>");
        out.println("</form>");
        out.println("<div style='margin-top:15px; text-align:center;'>");
        out.println("<a href='EquipmentListServlet'>Cancelar</a>");
        out.println("</div>");
        out.println("</div>");
        out.println(Utils.footer("Edit Equipment"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;
        if (role == null || !role.equalsIgnoreCase("admin")) {
            res.sendRedirect("index.html");
            return;
        }

        String idStr           = req.getParameter("id");
        String name            = req.getParameter("name");
        String type            = req.getParameter("type");
        String room            = req.getParameter("room");
        String status          = req.getParameter("status");
        String lastMaintenance = req.getParameter("lastMaintenance");
        String notes           = req.getParameter("notes");

        PrintWriter out = res.getWriter();

        int id;
        try { id = Integer.parseInt(idStr); }
        catch (Exception ex) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>id inv\u00e1lido.</div></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        if (name == null || name.trim().isEmpty()
            || type == null || type.trim().isEmpty()
            || status == null || status.trim().isEmpty()) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Nombre, tipo y estado son obligatorios.</div>");
            out.println("<a href='EditEquipmentServlet?id=" + id + "'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        if (lastMaintenance != null && lastMaintenance.trim().isEmpty()) {
            lastMaintenance = null;
        }

        int n = EquipmentData.update(connection, id, name.trim(), type, room, status, lastMaintenance, notes);
        if (n > 0) {
            res.sendRedirect("EquipmentListServlet");
        } else {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>No se pudo actualizar.</div></div>");
            out.println(Utils.footer("Error"));
            out.close();
        }
    }

    private void writeOption(PrintWriter out, String value, String current) {
        String sel = value.equals(current) ? " selected" : "";
        out.println("<option value='" + value + "'" + sel + ">" + value + "</option>");
    }

    private String attr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("'", "&#x27;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
