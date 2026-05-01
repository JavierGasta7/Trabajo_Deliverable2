import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class AddEquipmentServlet extends HttpServlet {
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
        out.println(Utils.header("A\u00f1adir equipo", session));

        out.println("<div class='card' style='max-width:560px; margin:30px auto;'>");
        out.println("<div class='title'>Nuevo equipo</div>");
        out.println("<form method='post' action='AddEquipmentServlet'>");
        out.println("<label>Nombre</label>");
        out.println("<input type='text' name='name' required>");
        out.println("<label>Tipo</label>");
        out.println("<select name='type' required>");
        out.println("<option value='cardio'>cardio</option>");
        out.println("<option value='fuerza'>fuerza</option>");
        out.println("<option value='libre'>libre</option>");
        out.println("</select>");
        out.println("<label>Sala</label>");
        out.println("<input type='text' name='room'>");
        out.println("<label>Estado</label>");
        out.println("<select name='status' required onchange='avisoEstadoBroken(this);'>");
        out.println("<option value='available'>available</option>");
        out.println("<option value='in_use'>in_use</option>");
        out.println("<option value='maintenance'>maintenance</option>");
        out.println("<option value='broken'>broken</option>");
        out.println("</select>");
        out.println("<label>Fecha de compra</label>");
        out.println("<input type='date' name='purchasedAt'>");
        out.println("<label>\u00daltima revisi\u00f3n</label>");
        out.println("<input type='date' name='lastMaintenance'>");
        out.println("<label>Notas <span id='notasCount' style='font-size:11px; color:#6b7280;'>0/200</span></label>");
        out.println("<input type='text' name='notes' maxlength='200' oninput='contarNotas(this,\"notasCount\");'>");
        out.println("<input type='submit' value='Crear equipo'>");
        out.println("</form>");
        out.println("<div style='margin-top:15px; text-align:center;'>");
        out.println("<a href='EquipmentListServlet'>Volver al listado</a>");
        out.println("</div>");
        out.println("</div>");

        out.println(Utils.footer("Add Equipment"));
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

        String name            = req.getParameter("name");
        String type            = req.getParameter("type");
        String room            = req.getParameter("room");
        String status          = req.getParameter("status");
        String purchasedAt     = req.getParameter("purchasedAt");
        String lastMaintenance = req.getParameter("lastMaintenance");
        String notes           = req.getParameter("notes");

        PrintWriter out = res.getWriter();

        if (name == null || name.trim().isEmpty()
            || type == null || type.trim().isEmpty()
            || status == null || status.trim().isEmpty()) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card' style='max-width:560px; margin:40px auto;'>");
            out.println("<div class='error'>Nombre, tipo y estado son obligatorios.</div>");
            out.println("<form method='post' action='AddEquipmentServlet'>");
            out.println("<label>Nombre</label>");
            out.println("<input type='text' name='name' value='" + attr(name) + "' required>");
            out.println("<label>Tipo</label>");
            out.println("<input type='text' name='type' value='" + attr(type) + "' required>");
            out.println("<label>Sala</label>");
            out.println("<input type='text' name='room' value='" + attr(room) + "'>");
            out.println("<label>Estado</label>");
            out.println("<input type='text' name='status' value='" + attr(status) + "' required>");
            out.println("<label>Fecha de compra</label>");
            out.println("<input type='date' name='purchasedAt' value='" + attr(purchasedAt) + "'>");
            out.println("<label>Última revisión</label>");
            out.println("<input type='date' name='lastMaintenance' value='" + attr(lastMaintenance) + "'>");
            out.println("<label>Notas</label>");
            out.println("<input type='text' name='notes' maxlength='200' value='" + attr(notes) + "'>");
            out.println("<input type='submit' value='Crear equipo'>");
            out.println("</form>");
            out.println("</div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        if (purchasedAt == null || purchasedAt.trim().isEmpty()) {
            purchasedAt = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
        if (lastMaintenance == null || lastMaintenance.trim().isEmpty()) {
            lastMaintenance = null;
        }

        int n = EquipmentData.insert(connection, name.trim(), type, room, status,
                                     purchasedAt, lastMaintenance, notes);
        if (n > 0) {
            res.sendRedirect("EquipmentListServlet");
        } else {
            out.println(Utils.header("Error", session));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='error'>No se pudo crear el equipo.</div>");
            out.println("<a href='AddEquipmentServlet'><button class='primary' type='button'>Volver</button></a>");
            out.println("</div>");
            out.println(Utils.footer("Error"));
            out.close();
        }
    }

    private String attr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("'", "&#x27;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
