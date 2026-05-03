import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class InstructorDashboardServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // 1. Configuración de caracteres
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter toClient = res.getWriter();

        // 2. Verificación de seguridad (Roles: instructor o manager)
        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;

        if (role == null || (!role.equalsIgnoreCase("instructor") && !role.equalsIgnoreCase("manager"))) {
            res.sendRedirect("index.html");
            return;
        }

        String userName = (String) session.getAttribute("userName");

        // 3. Generación de la vista
        toClient.println(Utils.header("Panel de Instructor", session));
        
        // El subheader ahora lo genera Utils, pero podemos añadir un saludo extra
        toClient.println("<div style='text-align:center; margin-top:10px;'>Holas, " + userName + ". Gestiona tus salas y clases.</div>");

        toClient.println("<div style='max-width: 900px; margin: 20px auto; display: flex; flex-wrap: wrap; justify-content: center; gap: 20px;'>");

        // TARJETA 1: Monitor de Ocupación (Trabajo de Ander)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Monitor de Salas</div>");
        toClient.println("        <p>Consulta el aforo en tiempo real y la ocupaci\u00f3n de las clases.</p>");
        toClient.println("        <a href='OccupancyMonitorServlet' style='display:block; text-align:center; margin-top:15px;'><button class='primary' type='button' style='width:auto; padding:10px 22px;'>Ver Ocupaci\u00f3n</button></a>");
        toClient.println("    </div>");

        // TARJETA 2: Catálogo de Clases (Vista/Edición)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Gesti\u00f3n de Clases</div>");
        toClient.println("        <p>Revisa el horario y realiza ajustes en las actividades.</p>");
        toClient.println("        <a href='ClassCatalogueServlet' style='display:block; text-align:center; margin-top:15px;'><button class='primary' type='button' style='width:auto; padding:10px 22px;'>Ver Cat\u00e1logo</button></a>");
        toClient.println("    </div>");

        // TARJETA 3: Inscritos en mis clases (Javier)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Inscritos en mis clases</div>");
        toClient.println("        <p>Consulta los miembros apuntados a cada una de tus clases.</p>");
        toClient.println("        <a href='ClassRosterServlet' style='display:block; text-align:center; margin-top:15px;'><button class='primary' type='button' style='width:auto; padding:10px 22px;'>Ver Inscritos</button></a>");
        toClient.println("    </div>");

        // TARJETA 4: Crear clase (Javier)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Crear clase</div>");
        toClient.println("        <p>Da de alta una nueva clase con fecha, sala y capacidad.</p>");
        toClient.println("        <a href='CreateClassServlet' style='display:block; text-align:center; margin-top:15px;'><button class='primary' type='button' style='width:auto; padding:10px 22px;'>Nueva Clase</button></a>");
        toClient.println("    </div>");

        toClient.println("</div>");

        // Botón de salida
        toClient.println("<div style='text-align: center; margin-top: 20px;'>");
        toClient.println("    <a href='LogoutServlet'><button class='secondary' type='button'>Cerrar Sesi\u00f3n</button></a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("Manager Panel"));
        toClient.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
}