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
        toClient.println("        <p>Consulta el aforo en tiempo real y la ocupación de las clases.</p>");
        toClient.println("        <a href='OccupancyMonitorServlet'><button class='primary' style='width:100%'>Ver Ocupación</button></a>");
        toClient.println("    </div>");

        // TARJETA 2: Catálogo de Clases (Vista/Edición)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Gestión de Clases</div>");
        toClient.println("        <p>Revisa el horario y realiza ajustes en las actividades.</p>");
        toClient.println("        <a href='ClassCatalogueServlet'><button class='primary' style='width:100%'>Ver Catálogo</button></a>");
        toClient.println("    </div>");

        toClient.println("</div>");

        // Botón de salida
        toClient.println("<div style='text-align: center; margin-top: 20px;'>");
        toClient.println("    <a href='LogoutServlet'><button class='secondary' type='button'>Cerrar Sesión</button></a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("Manager Panel"));
        toClient.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
}