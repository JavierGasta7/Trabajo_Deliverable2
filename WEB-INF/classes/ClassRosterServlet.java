import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class ClassRosterServlet extends HttpServlet {
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
        if (role == null || (!role.equalsIgnoreCase("instructor")
                          && !role.equalsIgnoreCase("manager")
                          && !role.equalsIgnoreCase("admin"))) {
            res.sendRedirect("index.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        PrintWriter out = res.getWriter();
        out.println(Utils.header("Inscritos en mis clases", session));

        String classIdStr = req.getParameter("classId");

        if (classIdStr == null) {
            Vector<ClassData> classes;
            if (role.equalsIgnoreCase("admin")) {
                classes = ClassData.getAvailableClasses(connection, null, null);
            } else {
                classes = ClassData.getClassesByInstructor(connection, userId);
            }

            out.println("<div class='card' style='max-width:1000px; margin:20px auto;'>");
            out.println("<div class='title'>Mis clases</div>");
            out.println("<div class='subtitle'>Selecciona una clase para ver los inscritos</div>");

            out.println("<table>");
            out.println("<tr><th>Clase</th><th>Actividad</th><th>Sala</th><th>Fecha</th><th>Hora</th><th>Plazas</th><th>Acci\u00f3n</th></tr>");
            for (int i = 0; i < classes.size(); i++) {
                ClassData c = classes.elementAt(i);
                out.println("<tr>");
                out.println("<td>" + safe(c.name) + "</td>");
                out.println("<td>" + safe(c.activityType) + "</td>");
                out.println("<td>" + safe(c.room) + "</td>");
                out.println("<td>" + safeDate(c.classDate) + "</td>");
                out.println("<td>" + safeTime(c.startTime) + "</td>");
                out.println("<td>" + c.bookedCount + "/" + c.maxCapacity + "</td>");
                out.println("<td><a href='ClassRosterServlet?classId=" + c.classId + "'>Ver inscritos</a></td>");
                out.println("</tr>");
            }
            if (classes.size() == 0) {
                out.println("<tr><td colspan='7' style='text-align:center; color:#6b7280;'>No tienes clases.</td></tr>");
            }
            out.println("</table>");
            out.println("</div>");
        } else {
            int classId;
            try { classId = Integer.parseInt(classIdStr); }
            catch (NumberFormatException ex) {
                out.println("<div class='card'><div class='error'>classId inv\u00e1lido.</div></div>");
                out.println(Utils.footer(""));
                out.close();
                return;
            }

            ClassData c = ClassData.getClass(connection, classId);
            if (c == null) {
                out.println("<div class='card'><div class='error'>Clase no encontrada.</div></div>");
                out.println(Utils.footer(""));
                out.close();
                return;
            }

            if (!role.equalsIgnoreCase("admin") && c.instructorId != userId) {
                out.println("<div class='card'><div class='error'>No puedes ver los inscritos de una clase que no impartes.</div>");
                out.println("<a href='ClassRosterServlet'><button class='primary' type='button'>Volver</button></a></div>");
                out.println(Utils.footer(""));
                out.close();
                return;
            }

            Vector<String[]> roster = BookingData.getRoster(connection, classId);

            out.println("<div class='card' style='max-width:1000px; margin:20px auto;'>");
            out.println("<div class='title'>" + safe(c.name) + "</div>");
            out.println("<div class='subtitle'>" + safe(c.activityType) + " \u00b7 " + safeDate(c.classDate) +
                        " " + safeTime(c.startTime) + " \u00b7 Sala " + safe(c.room) + "</div>");
            out.println("<p style='text-align:center; font-weight:bold; margin-top:10px;'>" +
                        roster.size() + " / " + c.maxCapacity + " inscritos</p>");

            out.println("<table>");
            out.println("<tr><th>Nombre</th><th>Email</th><th>Tel\u00e9fono</th><th>Reservado el</th></tr>");
            for (int i = 0; i < roster.size(); i++) {
                String[] r = roster.elementAt(i);
                out.println("<tr>");
                out.println("<td>" + safe(r[0]) + "</td>");
                out.println("<td>" + safe(r[1]) + "</td>");
                out.println("<td>" + safe(r[2]) + "</td>");
                out.println("<td>" + safe(r[3]) + "</td>");
                out.println("</tr>");
            }
            if (roster.size() == 0) {
                out.println("<tr><td colspan='4' style='text-align:center; color:#6b7280;'>A\u00fan no hay inscritos.</td></tr>");
            }
            out.println("</table>");

            out.println("<div style='text-align:center; margin-top:20px;'>");
            out.println("<a href='ClassRosterServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver a mis clases</button></a>");
            out.println("</div>");
            out.println("</div>");
        }

        out.println(Utils.footer("Class Roster"));
        out.close();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String safeDate(String s) {
        if (s == null || s.length() < 10) return s == null ? "" : s;
        return s.substring(0, 10);
    }

    private String safeTime(String s) {
        if (s == null) return "";
        if (s.length() >= 19) return s.substring(11, 19);
        return s;
    }
}
