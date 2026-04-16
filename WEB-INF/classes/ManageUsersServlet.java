import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class ManageUsersServlet extends HttpServlet {
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

        String roleFilter   = trim(req.getParameter("roleFilter"));
        String statusFilter = trim(req.getParameter("statusFilter"));
        String search       = trim(req.getParameter("search"));
        String message      = req.getParameter("message");

        int currentAdminId = (int) session.getAttribute("userId");

        Vector<UserData> users = UserData.getAllUsers(connection, roleFilter, statusFilter, search);

        int admins = 0, instructors = 0, members = 0, inactive = 0;
        for (int i = 0; i < users.size(); i++) {
            UserData u = users.elementAt(i);
            if ("admin".equalsIgnoreCase(u.role))           admins++;
            else if ("instructor".equalsIgnoreCase(u.role)) instructors++;
            else if ("member".equalsIgnoreCase(u.role))     members++;
            if (!"active".equalsIgnoreCase(u.membershipStatus)) inactive++;
        }

        PrintWriter out = res.getWriter();
        out.println(Utils.header("Gestionar usuarios", session));

        out.println("<div class='card' style='max-width:1100px; margin:20px auto;'>");
        out.println("<div class='title'>Gestionar usuarios</div>");
        out.println("<div class='subtitle'>Listado completo, filtros, cambio de rol y membres\u00eda</div>");

        if (message != null) {
            out.println("<div class='success' style='margin:10px 0;'>" + safe(message) + "</div>");
        }

        out.println("<div style='display:flex; gap:10px; justify-content:center; margin:15px 0; flex-wrap:wrap;'>");
        out.println(chip("Admins", admins, "#7c3aed"));
        out.println(chip("Instructores", instructors, "#2563eb"));
        out.println(chip("Miembros", members, "#10b981"));
        out.println(chip("Inactivos", inactive, "#ef4444"));
        out.println("</div>");

        out.println("<form method='get' action='ManageUsersServlet' " +
                    "style='display:flex; gap:10px; flex-wrap:wrap; align-items:end; margin:15px 0;'>");
        out.println("<div><label>Rol</label>");
        out.println("<select name='roleFilter'>");
        out.println(opt("",           "Todos",       roleFilter));
        out.println(opt("admin",      "Admin",       roleFilter));
        out.println(opt("instructor", "Instructor",  roleFilter));
        out.println(opt("member",     "Miembro",     roleFilter));
        out.println("</select></div>");
        out.println("<div><label>Estado</label>");
        out.println("<select name='statusFilter'>");
        out.println(opt("",         "Todos",    statusFilter));
        out.println(opt("active",   "Activo",   statusFilter));
        out.println(opt("inactive", "Inactivo", statusFilter));
        out.println("</select></div>");
        out.println("<div style='flex:1; min-width:200px;'><label>Buscar</label>");
        out.println("<input type='text' name='search' value='" + attr(search) + "' placeholder='Nombre o email'></div>");
        out.println("<div><input type='submit' value='Filtrar' style='width:auto; padding:8px 16px;'></div>");
        out.println("</form>");

        out.println("<table>");
        out.println("<tr><th>ID</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr>");
        for (int i = 0; i < users.size(); i++) {
            UserData u = users.elementAt(i);
            boolean isSelf = (u.userId == currentAdminId);

            out.println("<tr>");
            out.println("<td>" + u.userId + "</td>");
            out.println("<td>" + safe(u.fullName) + (isSelf ? " <span style='color:#7c3aed; font-size:11px;'>(t\u00fa)</span>" : "") + "</td>");
            out.println("<td>" + safe(u.email) + "</td>");
            out.println("<td>" + roleBadge(u.role) + "</td>");
            out.println("<td>" + statusBadge(u.membershipStatus) + "</td>");
            out.println("<td>");

            if (isSelf) {
                out.println("<span style='color:#6b7280; font-size:12px;'>No editable</span>");
            } else {
                out.println("<form method='post' action='ManageUsersServlet' style='display:inline; margin-right:6px;'>");
                out.println("<input type='hidden' name='action' value='role'>");
                out.println("<input type='hidden' name='userId' value='" + u.userId + "'>");
                out.println("<select name='newRole' style='padding:4px; font-size:12px;'>");
                out.println(opt("admin",      "Admin",       u.role));
                out.println(opt("instructor", "Instructor",  u.role));
                out.println(opt("member",     "Miembro",     u.role));
                out.println("</select>");
                out.println("<button class='primary' type='submit' style='width:auto; padding:4px 10px; font-size:12px;'>Cambiar</button>");
                out.println("</form>");

                String toggleTo    = "active".equalsIgnoreCase(u.membershipStatus) ? "inactive" : "active";
                String toggleLabel = "active".equalsIgnoreCase(u.membershipStatus) ? "Desactivar" : "Activar";
                out.println("<form method='post' action='ManageUsersServlet' style='display:inline;'>");
                out.println("<input type='hidden' name='action' value='status'>");
                out.println("<input type='hidden' name='userId' value='" + u.userId + "'>");
                out.println("<input type='hidden' name='newStatus' value='" + toggleTo + "'>");
                out.println("<button class='secondary' type='submit' style='width:auto; padding:4px 10px; font-size:12px;'>" + toggleLabel + "</button>");
                out.println("</form>");
            }

            out.println("</td>");
            out.println("</tr>");
        }
        if (users.size() == 0) {
            out.println("<tr><td colspan='6' style='text-align:center; color:#6b7280;'>No hay usuarios con esos filtros.</td></tr>");
        }
        out.println("</table>");

        out.println("<div style='text-align:center; margin-top:20px;'>");
        out.println("<a href='AdminDashboardServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver al panel</button></a>");
        out.println("</div>");
        out.println("</div>");

        out.println(Utils.footer("Manage Users"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;
        if (role == null || !role.equalsIgnoreCase("admin")) {
            res.sendRedirect("index.html");
            return;
        }

        int currentAdminId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");

        int userId;
        try { userId = Integer.parseInt(req.getParameter("userId")); }
        catch (Exception e) {
            res.sendRedirect("ManageUsersServlet?message=ID+inv" + "\u00e1" + "lido");
            return;
        }

        if (userId == currentAdminId) {
            res.sendRedirect("ManageUsersServlet?message=No+puedes+modificar+tu+propia+cuenta");
            return;
        }

        String message;
        if ("role".equals(action)) {
            String newRole = req.getParameter("newRole");
            if (newRole == null || !(newRole.equals("admin") || newRole.equals("instructor")
                || newRole.equals("member"))) {
                message = "Rol no v\u00e1lido";
            } else {
                int n = UserData.updateRole(connection, userId, newRole);
                message = (n > 0) ? "Rol actualizado correctamente" : "No se pudo actualizar el rol";
            }
        } else if ("status".equals(action)) {
            String newStatus = req.getParameter("newStatus");
            if (!"active".equals(newStatus) && !"inactive".equals(newStatus)) {
                message = "Estado no v\u00e1lido";
            } else {
                int n = UserData.updateMembershipStatus(connection, userId, newStatus);
                message = (n > 0) ? "Estado actualizado correctamente" : "No se pudo actualizar el estado";
            }
        } else {
            message = "Acci\u00f3n desconocida";
        }

        res.sendRedirect("ManageUsersServlet?message=" + java.net.URLEncoder.encode(message, "UTF-8"));
    }

    private String chip(String label, int count, String color) {
        return "<div style='padding:10px 16px; border-radius:8px; background:" + color + "; color:white; min-width:100px; text-align:center;'>" +
               "<div style='font-size:22px; font-weight:bold;'>" + count + "</div>" +
               "<div style='font-size:12px;'>" + label + "</div></div>";
    }

    private String roleBadge(String r) {
        String c = "#6b7280";
        if ("admin".equalsIgnoreCase(r))           c = "#7c3aed";
        else if ("instructor".equalsIgnoreCase(r)) c = "#2563eb";
        else if ("manager".equalsIgnoreCase(r))    c = "#0891b2";
        else if ("member".equalsIgnoreCase(r))     c = "#10b981";
        return "<span style='background:" + c + "; color:white; padding:3px 8px; border-radius:4px; font-size:11px;'>" +
               safe(r == null ? "-" : r) + "</span>";
    }

    private String statusBadge(String s) {
        boolean active = "active".equalsIgnoreCase(s);
        String c = active ? "#10b981" : "#ef4444";
        String t = active ? "Activo" : (s == null ? "-" : "Inactivo");
        return "<span style='background:" + c + "; color:white; padding:3px 8px; border-radius:4px; font-size:11px;'>" + t + "</span>";
    }

    private String opt(String v, String label, String current) {
        String sel = (current != null && current.equalsIgnoreCase(v)) ? " selected" : "";
        return "<option value='" + attr(v) + "'" + sel + ">" + safe(label) + "</option>";
    }

    private String trim(String s) { return (s == null) ? null : s.trim(); }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String attr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("'", "&#x27;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
