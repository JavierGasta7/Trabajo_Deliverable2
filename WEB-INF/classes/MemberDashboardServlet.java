import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class MemberDashboardServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
		req.setCharacterEncoding("UTF-8");
        PrintWriter toClient = res.getWriter();

        // 1. Verificación de sesión (Seguridad)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userRole") == null) {
            res.sendRedirect("index.html");
            return;
        }

        // Recuperamos el nombre del usuario de la sesión para saludarle
        String userName = (String) session.getAttribute("userName");

        // 2. Generación de la página usando tus clases Utils y style.css
        toClient.println(Utils.header("Mi Panel - GymTrack",session));
		
		toClient.println("<meta charset='UTF-8'>");
        
        // Mensaje de bienvenida
        toClient.println("<div class='subheader'>Bienvenido/a, " + userName + "</div>");

        // Contenedor para las tarjetas (usando el estilo de tu CSS)
        toClient.println("<div style='max-width: 900px; margin: 20px auto; display: flex; flex-wrap: wrap; justify-content: center; gap: 20px;'>");

        // TARJETA 1: Explorar Clases (Trabajo de Marco)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Reservar Clase</div>");
        toClient.println("        <p>Consulta los horarios disponibles y ap\u00fantate.</p>");
        toClient.println("        <a href='browseClasses.html'><button class='primary' style='width:100%'>Ver Clases</button></a>");
        toClient.println("    </div>");

        // TARJETA 2: Mis Reservas (Trabajo de Marco)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Mi Historial</div>");
        toClient.println("        <p>Revisa tus clases programadas o cancela una reserva.</p>");
        toClient.println("        <a href='bookingHistory.html'><button class='primary' style='width:100%'>Ver Mis Citas</button></a>");
        toClient.println("    </div>");

        // TARJETA 3: Valorar (Trabajo de Ander/Compañeros)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Tu Opini\u00f3n</div>");
        toClient.println("        <p>\u00bfQu\u00e9 te pareci\u00f3 la clase? Ay\u00fadanos a mejorar.</p>");
        toClient.println("        <a href='RateClassServlet'><button class='primary' style='width:100%'>Valorar Clases</button></a>");
        toClient.println("    </div>");

        // TARJETA 4: Editar perfil (Javier)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Editar Perfil</div>");
        toClient.println("        <p>Actualiza tu nombre, tel\u00e9fono y fecha de nacimiento.</p>");
        toClient.println("        <a href='EditProfileServlet'><button class='primary' style='width:100%'>Mi Perfil</button></a>");
        toClient.println("    </div>");

        // TARJETA 5: Mis estadísticas (Javier)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Mis Estad\u00edsticas</div>");
        toClient.println("        <p>Consulta tu tasa de asistencia, actividad favorita y medalla de fidelidad.</p>");
        toClient.println("        <a href='PersonalStatsServlet'><button class='primary' style='width:100%'>Ver Estad\u00edsticas</button></a>");
        toClient.println("    </div>");

        toClient.println("</div>");

        // Botón de salida
        toClient.println("<div style='text-align: center; margin-top: 20px;'>");
        toClient.println("    <a href='LogoutServlet'><button class='secondary' type='button'>Cerrar Sesi\u00f3n</button></a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("Mi Panel"));
        
        toClient.close();
    }

    // El método doPost simplemente llama al doGet
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
}