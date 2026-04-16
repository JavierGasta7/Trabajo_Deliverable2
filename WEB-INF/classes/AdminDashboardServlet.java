import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class AdminDashboardServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
		req.setCharacterEncoding("UTF-8");
        PrintWriter toClient = res.getWriter();

        // 1. Verificación de seguridad: Solo admins
        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;

        if (role == null || !role.equalsIgnoreCase("admin")) {
            res.sendRedirect("index.html");
            return;
        }

        String userName = (String) session.getAttribute("userName");

        // 2. Generación de la vista profesional
        toClient.println(Utils.header("Panel de Administraci\u00f3n", session));
        toClient.println("<div class='subheader'>Control Global: " + userName + " (Administrador)</div>");

        toClient.println("<div style='max-width: 900px; margin: 20px auto; display: flex; flex-wrap: wrap; justify-content: center; gap: 20px;'>");

        // TARJETA 1: Gestión de Usuarios (Aprobaciones)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Control de Usuarios</div>");
        toClient.println("        <p>Aprobar nuevos registros o gestionar estados de membres\u00eda.</p>");
        // Aquí puedes poner el enlace al servlet que gestiona usuarios o recargar esta misma página con parámetros
        toClient.println("        <a href='ManageUsersServlet'><button class='primary' style='width:100%'>Gestionar Usuarios</button></a>");
        toClient.println("    </div>");

        // TARJETA 2: Catálogo Maestro (Trabajo de Ander)
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Cat\u00e1logo de Clases</div>");
        toClient.println("        <p>A\u00f1adir, editar o cancelar clases del gimnasio.</p>");
        toClient.println("        <a href='ClassCatalogueServlet'><button class='primary' style='width:100%'>Editar Cat\u00e1logo</button></a>");
        toClient.println("    </div>");

        // TARJETA 3: Monitor de Ocupación
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>M\u00e9tricas en Vivo</div>");
        toClient.println("        <p>Ver el estado de ocupaci\u00f3n de todas las salas.</p>");
        toClient.println("        <a href='OccupancyMonitorServlet'><button class='primary' style='width:100%'>Abrir Monitor</button></a>");
        toClient.println("    </div>");

        // TARJETA 4: Equipamiento
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Equipamiento</div>");
        toClient.println("        <p>Gestionar m\u00e1quinas y material del gimnasio.</p>");
        toClient.println("        <a href='EquipmentListServlet'><button class='primary' style='width:100%'>Ver Equipamiento</button></a>");
        toClient.println("    </div>");

        // TARJETA 5: Alertas de mantenimiento
        toClient.println("    <div class='card' style='width: 280px;'>");
        toClient.println("        <div class='title'>Alertas de Mantenimiento</div>");
        toClient.println("        <p>Equipos rotos o con revisi\u00f3n pendiente.</p>");
        toClient.println("        <a href='MaintenanceAlertServlet'><button class='primary' style='width:100%'>Ver Alertas</button></a>");
        toClient.println("    </div>");

        toClient.println("</div>");

        // Botón de salida
        toClient.println("<div style='text-align: center; margin-top: 20px;'>");
        toClient.println("    <a href='LogoutServlet'><button class='secondary' type='button'>Cerrar Sesi\u00f3n</button></a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("Admin Panel"));
        toClient.close();
    }
}