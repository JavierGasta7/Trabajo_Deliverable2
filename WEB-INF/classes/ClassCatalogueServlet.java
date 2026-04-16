import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class ClassCatalogueServlet extends HttpServlet {
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
        String role = (String) session.getAttribute("userRole");
        if (!"admin".equalsIgnoreCase(role) && !"instructor".equalsIgnoreCase(role) && !"manager".equalsIgnoreCase(role)) {
            res.sendRedirect("index.html");
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String action  = req.getParameter("action");
        String idParam = req.getParameter("id");
        PrintWriter out = res.getWriter();

        out.println(Utils.header("Gesti\u00f3n del Cat\u00e1logo", session));

        try {
            if ("edit".equals(action) && idParam != null) {
                int id = Integer.parseInt(idParam);
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, name, activity_type, room, class_date, start_time, duration_min, max_capacity FROM classes WHERE id=?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.println("<div class='card' style='max-width:600px; margin:20px auto;'>");
                    out.println("<div class='title'>Editar clase</div>");
                    out.println("<form method='post' action='ClassCatalogueServlet'>");
                    out.println("<input type='hidden' name='action' value='edit'>");
                    out.println("<input type='hidden' name='id' value='" + id + "'>");

                    out.println("<label>Nombre</label>");
                    out.println("<input type='text' name='name' value='" + attr(rs.getString("name")) + "' required>");

                    out.println("<label>Tipo de actividad</label>");
                    out.println("<input type='text' name='activityType' value='" + attr(rs.getString("activity_type")) + "' required>");

                    out.println("<label>Sala</label>");
                    out.println("<input type='text' name='room' value='" + attr(rs.getString("room")) + "' required>");

                    out.println("<label>Fecha</label>");
                    out.println("<input type='date' name='classDate' value='" + Utils.formatDate(rs.getString("class_date")) + "' required>");

                    out.println("<label>Hora de inicio</label>");
                    out.println("<input type='time' name='startTime' value='" + Utils.formatTime(rs.getString("start_time")) + "' required>");

                    out.println("<label>Duraci\u00f3n (minutos)</label>");
                    out.println("<input type='number' name='durationMin' min='15' max='240' value='" + rs.getInt("duration_min") + "' required>");

                    out.println("<label>Capacidad m\u00e1xima</label>");
                    out.println("<input type='number' name='maxCapacity' min='1' max='100' value='" + rs.getInt("max_capacity") + "' required>");

                    out.println("<input type='submit' value='Guardar cambios'>");
                    out.println("</form>");
                    out.println("<div style='text-align:center; margin-top:10px;'>");
                    out.println("<a href='ClassCatalogueServlet'>Cancelar</a>");
                    out.println("</div>");
                    out.println("</div>");
                }
                rs.close();
                ps.close();
            }

            PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name, activity_type, room, class_date, start_time, duration_min, max_capacity " +
                "FROM classes ORDER BY class_date DESC, start_time DESC");
            ResultSet rs = ps.executeQuery();

            out.println("<div class='card' style='max-width:1000px; margin:20px auto;'>");
            out.println("<div class='title'>Cat\u00e1logo de clases</div>");
            out.println("<div class='subtitle'>Gestiona horarios, salas y capacidad</div>");

            out.println("<table>");
            out.println("<tr><th>Clase</th><th>Actividad</th><th>Sala</th><th>Fecha</th><th>Hora</th><th>Duraci\u00f3n</th><th>Capacidad</th><th>Acciones</th></tr>");

            boolean any = false;
            while (rs.next()) {
                any = true;
                int id = rs.getInt("id");
                out.println("<tr>");
                out.println("<td>" + safe(rs.getString("name")) + "</td>");
                out.println("<td>" + safe(rs.getString("activity_type")) + "</td>");
                out.println("<td>" + safe(rs.getString("room")) + "</td>");
                out.println("<td>" + Utils.formatDate(rs.getString("class_date")) + "</td>");
                out.println("<td>" + Utils.formatTime(rs.getString("start_time")) + "</td>");
                out.println("<td>" + rs.getInt("duration_min") + " min</td>");
                out.println("<td>" + rs.getInt("max_capacity") + "</td>");
                out.println("<td style='white-space:nowrap;'>");
                out.println("<a href='ClassCatalogueServlet?action=edit&id=" + id + "'>Editar</a> &middot; ");
                out.println("<form method='post' action='ClassCatalogueServlet' style='display:inline;'>");
                out.println("<input type='hidden' name='action' value='cancel'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<button type='submit' style='background:none; border:none; color:#dc3545; cursor:pointer; padding:0; font:inherit;' " +
                            "onclick=\"return confirm('\u00bfSeguro que quieres cancelar todas las reservas de esta clase?')\">Cancelar reservas</button>");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }
            if (!any) {
                out.println("<tr><td colspan='8' style='text-align:center; color:#6b7280;'>No hay clases en el cat\u00e1logo.</td></tr>");
            }
            out.println("</table>");

            out.println("<div style='text-align:center; margin-top:20px;'>");
            if ("admin".equalsIgnoreCase(role)) {
                out.println("<a href='AdminDashboardServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver al panel</button></a>");
            } else {
                out.println("<a href='InstructorDashboardServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver al panel</button></a>");
            }
            out.println("</div>");
            out.println("</div>");
            rs.close();
            ps.close();

        } catch (Exception e) {
            out.println("<div class='card'><div class='error'>Error al cargar el cat\u00e1logo: " + safe(e.getMessage()) + "</div></div>");
            e.printStackTrace();
        }

        out.println(Utils.footer("Catalogue"));
        out.close();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;
        if (role == null || (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("instructor") && !role.equalsIgnoreCase("manager"))) {
            res.sendRedirect("index.html");
            return;
        }

        String action  = req.getParameter("action");
        String idParam = req.getParameter("id");

        try {
            int id = Integer.parseInt(idParam);

            if ("cancel".equals(action)) {
                PreparedStatement ps = connection.prepareStatement(
                    "UPDATE bookings SET status='cancelled' WHERE class_id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
            } else if ("edit".equals(action)) {
                String name         = req.getParameter("name");
                String activityType = req.getParameter("activityType");
                String room         = req.getParameter("room");
                String classDate    = req.getParameter("classDate");
                String startTime    = req.getParameter("startTime");
                String durationStr  = req.getParameter("durationMin");
                String capacityStr  = req.getParameter("maxCapacity");

                if (startTime != null && startTime.length() == 5) startTime += ":00";

                int duration = Integer.parseInt(durationStr);
                int capacity = Integer.parseInt(capacityStr);

                PreparedStatement ps = connection.prepareStatement(
                    "UPDATE classes SET name=?, activity_type=?, room=?, class_date=?, start_time=?, duration_min=?, max_capacity=? WHERE id=?");
                ps.setString(1, name);
                ps.setString(2, activityType);
                ps.setString(3, room);
                ps.setString(4, classDate);
                ps.setString(5, startTime);
                ps.setInt(6, duration);
                ps.setInt(7, capacity);
                ps.setInt(8, id);
                ps.executeUpdate();
                ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        res.sendRedirect("ClassCatalogueServlet");
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String attr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("'", "&#x27;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
