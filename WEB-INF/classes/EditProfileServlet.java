import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class EditProfileServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");
        UserData user = UserData.getUser(connection, userId);

        PrintWriter out = res.getWriter();
        out.println(Utils.header("Editar perfil", session));

        if (user == null) {
            out.println("<div class='card'><div class='error'>No se pudo cargar tu perfil.</div></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        out.println("<div class='card' style='max-width:520px; margin:30px auto;'>");
        out.println("<div class='title'>Mi perfil</div>");
        out.println("<div class='subtitle'>Actualiza tus datos personales</div>");
        out.println("<form method='post' action='EditProfileServlet'>");
        out.println("<label>Nombre completo</label>");
        out.println("<input type='text' name='fullName' value='" + attr(user.fullName) + "' required>");
        out.println("<label>Correo (no editable)</label>");
        out.println("<input type='email' value='" + attr(user.email) + "' disabled style='background:#f3f4f6;'>");
        out.println("<label>Tel\u00e9fono</label>");
        out.println("<input type='text' name='phone' value='" + getPhoneValue(connection, userId) + "' required>");
        out.println("<label>Fecha de nacimiento</label>");
        out.println("<input type='date' name='dateOfBirth' value='" + getDobValue(connection, userId) + "' required>");
        out.println("<input type='submit' value='Guardar cambios'>");
        out.println("</form>");
        out.println("<div style='margin-top:15px; text-align:center;'>");
        out.println("<a href='" + dashboardForRole(user.role) + "'>Volver al panel</a>");
        out.println("</div>");
        out.println("</div>");
        out.println(Utils.footer("Edit Profile"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        String fullName    = req.getParameter("fullName");
        String phone       = req.getParameter("phone");
        String dateOfBirth = req.getParameter("dateOfBirth");

        PrintWriter out = res.getWriter();

        if (fullName == null || fullName.trim().isEmpty()
            || phone == null || phone.trim().isEmpty()
            || dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Todos los campos son obligatorios.</div>");
            out.println("<a href='EditProfileServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        int n = UserData.updateProfile(connection, userId, fullName.trim(), phone.trim(), dateOfBirth);
        if (n > 0) {
            session.setAttribute("userName", fullName.trim());
            String role = (String) session.getAttribute("userRole");

            out.println(Utils.header("Perfil actualizado", session));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='success'>Tus datos se han guardado correctamente.</div>");
            out.println("<a href='" + dashboardForRole(role) + "'><button class='primary' type='button'>Volver al panel</button></a>");
            out.println("<a href='EditProfileServlet'><button class='secondary' type='button'>Seguir editando</button></a>");
            out.println("</div>");
            out.println(Utils.footer("Updated"));
        } else {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>No se pudo actualizar el perfil.</div>");
            out.println("<a href='EditProfileServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
        }
        out.close();
    }

    private String dashboardForRole(String role) {
        if (role == null) return "index.html";
        if (role.equalsIgnoreCase("admin")) return "AdminDashboardServlet";
        if (role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("manager")) return "InstructorDashboardServlet";
        return "MemberDashboardServlet";
    }

    private String getPhoneValue(Connection conn, int userId) {
        try {
            java.sql.PreparedStatement p = conn.prepareStatement("SELECT phone FROM users WHERE id=?");
            p.setInt(1, userId);
            java.sql.ResultSet r = p.executeQuery();
            String v = r.next() ? r.getString("phone") : "";
            r.close(); p.close();
            return attr(v == null ? "" : v);
        } catch (Exception e) { return ""; }
    }

    private String getDobValue(Connection conn, int userId) {
        try {
            java.sql.PreparedStatement p = conn.prepareStatement("SELECT date_of_birth FROM users WHERE id=?");
            p.setInt(1, userId);
            java.sql.ResultSet r = p.executeQuery();
            String v = r.next() ? r.getString("date_of_birth") : "";
            r.close(); p.close();
            if (v == null) return "";
            if (v.length() >= 10) return v.substring(0, 10);
            return v;
        } catch (Exception e) { return ""; }
    }

    private String attr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("'", "&#x27;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
