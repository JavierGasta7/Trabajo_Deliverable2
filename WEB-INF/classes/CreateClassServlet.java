import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class CreateClassServlet extends HttpServlet {
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

        PrintWriter out = res.getWriter();
        out.println(Utils.header("Crear clase", session));
        out.println("<div class='card' style='max-width:560px; margin:30px auto;'>");
        out.println("<div class='title'>Nueva clase</div>");
        out.println("<form method='post' action='CreateClassServlet'>");
        out.println("<label>Nombre de la clase</label>");
        out.println("<input type='text' name='name' required>");
        out.println("<label>Tipo de actividad</label>");
        out.println("<input type='text' name='activityType' placeholder='Yoga, Spinning, CrossFit...' required>");
        out.println("<label>Sala</label>");
        out.println("<input type='text' name='room' required>");
        out.println("<label>Fecha</label>");
        out.println("<input type='date' name='classDate' required>");
        out.println("<label>Hora de inicio</label>");
        out.println("<input type='time' name='startTime' required>");
        out.println("<label>Duraci\u00f3n (minutos)</label>");
        out.println("<input type='number' name='durationMin' min='15' max='240' value='60' required>");
        out.println("<label>Capacidad m\u00e1xima</label>");
        out.println("<input type='number' name='maxCapacity' min='1' max='100' value='20' required>");
        out.println("<input type='submit' value='Crear clase'>");
        out.println("</form>");
        out.println("<div style='margin-top:15px; text-align:center;'>");
        out.println("<a href='InstructorDashboardServlet'>Cancelar</a>");
        out.println("</div>");
        out.println("</div>");
        out.println(Utils.footer("Create Class"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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

        int instructorId = (int) session.getAttribute("userId");

        String name         = req.getParameter("name");
        String activityType = req.getParameter("activityType");
        String room         = req.getParameter("room");
        String classDate    = req.getParameter("classDate");
        String startTime    = req.getParameter("startTime");
        String durationStr  = req.getParameter("durationMin");
        String capacityStr  = req.getParameter("maxCapacity");

        PrintWriter out = res.getWriter();

        if (name == null || name.trim().isEmpty()
            || activityType == null || activityType.trim().isEmpty()
            || room == null || room.trim().isEmpty()
            || classDate == null || classDate.trim().isEmpty()
            || startTime == null || startTime.trim().isEmpty()) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Todos los campos son obligatorios.</div>");
            out.println("<a href='CreateClassServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        int durationMin, maxCapacity;
        try {
            durationMin = Integer.parseInt(durationStr);
            maxCapacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException ex) {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>Duraci\u00f3n y capacidad deben ser n\u00fameros.</div></div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

		if (startTime.length() == 5) startTime += ":00";
			classDate = classDate + " 00:00:00";
			startTime = "1899-12-30 " + startTime;

			int n = ClassData.insertClass(connection, name.trim(), activityType.trim(), room.trim(),
                              classDate, startTime, durationMin, maxCapacity, instructorId);
        if (n > 0) {
            out.println(Utils.header("Clase creada", session));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='success'>Clase creada correctamente.</div>");
            out.println("<p><b>" + name + "</b> (" + activityType + ") el " + classDate + " a las " + startTime + "</p>");
            out.println("<a href='ClassRosterServlet'><button class='primary' type='button'>Ver mis clases</button></a>");
            out.println("<a href='CreateClassServlet'><button class='secondary' type='button'>Crear otra</button></a>");
            out.println("</div>");
            out.println(Utils.footer("Created"));
        } else {
            out.println(Utils.header("Error", session));
            out.println("<div class='card'><div class='error'>No se pudo crear la clase.</div>");
            out.println("<a href='CreateClassServlet'><button class='primary' type='button'>Volver</button></a></div>");
            out.println(Utils.footer("Error"));
        }
        out.close();
    }
}
