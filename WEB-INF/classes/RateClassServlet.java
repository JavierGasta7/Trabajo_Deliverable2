import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class RateClassServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtils.checkSession(req, res)) return;

        BookingData.completeExpiredBookings(connection);

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        String classIdParam = req.getParameter("classId");
        PrintWriter out = res.getWriter();
        out.println(Utils.header("Valorar clases", session));

        if (classIdParam == null) {
            Vector<BookingData> rateable = BookingData.getRateableClasses(connection, userId);

            out.println("<div class='card' style='max-width:900px; margin:20px auto;'>");
            out.println("<div class='title'>Valorar mis clases</div>");
            out.println("<div class='subtitle'>Clases a las que asististe y a\u00fan no has valorado</div>");

            if (rateable.size() == 0) {
                out.println("<div style='text-align:center; padding:30px; color:#6b7280;'>");
                out.println("<p>No tienes clases pendientes de valorar.</p>");
                out.println("<p style='font-size:13px;'>Solo puedes valorar clases con reserva confirmada que ya hayan terminado.</p>");
                out.println("</div>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Clase</th><th>Actividad</th><th>Instructor</th><th>Fecha</th><th>Hora</th><th>Acci\u00f3n</th></tr>");
                for (int i = 0; i < rateable.size(); i++) {
                    BookingData b = rateable.elementAt(i);
                    out.println("<tr>");
                    out.println("<td>" + safe(b.className) + "</td>");
                    out.println("<td>" + safe(b.activityType) + "</td>");
                    out.println("<td>" + safe(b.instructorName) + "</td>");
                    out.println("<td>" + safeDate(b.classDate) + "</td>");
                    out.println("<td>" + safeTime(b.startTime) + "</td>");
                    out.println("<td><a href='RateClassServlet?classId=" + b.classId + "'>Valorar</a></td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }

            out.println("<div style='text-align:center; margin-top:20px;'>");
            out.println("<a href='MemberDashboardServlet'><button class='secondary' type='button' style='width:auto; padding:8px 16px;'>Volver al panel</button></a>");
            out.println("</div>");
            out.println("</div>");
            out.println(Utils.footer("Rate Class"));
            out.close();
            return;
        }

        int classId;
        try { classId = Integer.parseInt(classIdParam); }
        catch (NumberFormatException ex) {
            out.println("<div class='card'><div class='error'>classId inv\u00e1lido.</div></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        if (!BookingData.canMemberRate(connection, userId, classId)) {
            out.println("<div class='card' style='max-width:520px; margin:40px auto;'>");
            out.println("<div class='error'>No puedes valorar esta clase (sin reserva confirmada o a\u00fan no ha terminado).</div>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Volver al listado</button></a>");
            out.println("</div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }
        if (BookingData.hasRated(connection, userId, classId)) {
            out.println("<div class='card' style='max-width:520px; margin:40px auto;'>");
            out.println("<div class='error'>Ya has valorado esta clase.</div>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Volver al listado</button></a>");
            out.println("</div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        ClassData c = ClassData.getClass(connection, classId);

        out.println("<div class='card' style='max-width:560px; margin:30px auto;'>");
        out.println("<div class='title'>Valorar clase</div>");
        if (c != null) {
            out.println("<div class='subtitle'>" + safe(c.name) + " \u00b7 " + safe(c.activityType) +
                        " \u00b7 " + safeDate(c.classDate) + "</div>");
        }
        out.println("<form method='post' action='RateClassServlet'>");
        out.println("<input type='hidden' name='classId' value='" + classId + "'>");

        out.println("<label>Puntuaci\u00f3n</label>");
        out.println("<div style='display:flex; gap:10px; justify-content:center; margin:10px 0;'>");
        for (int s = 1; s <= 5; s++) {
            out.println("<label style='cursor:pointer; font-size:22px;'>");
            out.println("<input type='radio' name='stars' value='" + s + "' required style='margin-right:4px;'>");
            out.println(s + " \u2605</label>");
        }
        out.println("</div>");

        out.println("<label>Comentario (opcional) <span id='comentCount' style='font-size:11px; color:#6b7280;'>0/500</span></label>");
        out.println("<textarea name='comment' rows='4' style='width:100%;' maxlength='500' placeholder='Cu\u00e9ntanos qu\u00e9 te pareci\u00f3' oninput='contarComentario(this,\"comentCount\")'></textarea>");

        out.println("<input type='submit' value='Enviar valoraci\u00f3n'>");
        out.println("</form>");
        out.println("<div style='margin-top:10px; text-align:center;'>");
        out.println("<a href='RateClassServlet'>Volver al listado</a>");
        out.println("</div>");
        out.println("</div>");

        out.println(Utils.footer("Rate Class"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtils.checkSession(req, res)) return;

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        PrintWriter out = res.getWriter();

        int classId, stars;
        try {
            classId = Integer.parseInt(req.getParameter("classId"));
            stars   = Integer.parseInt(req.getParameter("stars"));
        } catch (Exception e) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Datos inv\u00e1lidos.</div></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        String comment = req.getParameter("comment");

        out.println(Utils.header("Valoraci\u00f3n", session));

        if (stars < 1 || stars > 5) {
            out.println("<div class='card'><div class='error'>La puntuaci\u00f3n debe estar entre 1 y 5.</div>");
            out.println("<a href='RateClassServlet?classId=" + classId + "'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        if (!BookingData.canMemberRate(connection, userId, classId)) {
            out.println("<div class='card'><div class='error'>No puedes valorar esta clase.</div>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }
        if (BookingData.hasRated(connection, userId, classId)) {
            out.println("<div class='card'><div class='error'>Ya has valorado esta clase.</div>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer(""));
            out.close();
            return;
        }

        int n = BookingData.insertRating(connection, userId, classId, stars, comment);
        if (n > 0) {
            double avg = BookingData.getAverageStars(connection, classId);
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='success'>\u00a1Gracias por tu valoraci\u00f3n!</div>");
            out.println("<p style='text-align:center; font-size:28px;'>");
            for (int s = 1; s <= 5; s++) out.print(s <= stars ? "\u2605" : "\u2606");
            out.println("</p>");
            out.println("<p style='text-align:center; color:#6b7280;'>Media de la clase: <b>" + String.format("%.2f", avg) + "</b> / 5</p>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Valorar otra clase</button></a>");
            out.println("<a href='MemberDashboardServlet'><button class='secondary' type='button'>Volver al panel</button></a>");
            out.println("</div>");
        } else {
            out.println("<div class='card'><div class='error'>No se pudo guardar la valoraci\u00f3n.</div>");
            out.println("<a href='RateClassServlet'><button class='primary' type='button'>Volver</button></a></div>");
        }

        out.println(Utils.footer("Rate Class"));
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
