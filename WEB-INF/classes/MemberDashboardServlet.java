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
        toClient.println("        <p>Consulta los horarios disponibles y apúntate.</p>");
        toClient.println("        <a href='BrowseClassesServlet'><button class='primary' style='width:100%'>Ver Clases</button></a>");
        toClient.println("    </div>");

        // TARJETA 2: Mis Reservas (Trabajo de Marco)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Mi Historial</div>");
        toClient.println("        <p>Revisa tus clases programadas o cancela una reserva.</p>");
        toClient.println("        <a href='BookingHistoryServlet'><button class='primary' style='width:100%'>Ver Mis Citas</button></a>");
        toClient.println("    </div>");

        // TARJETA 3: Valorar (Trabajo de Ander/Compañeros)
        toClient.println("    <div class='card' style='width: 250px;'>");
        toClient.println("        <div class='title'>Tu Opinión</div>");
        toClient.println("        <p>¿Qué te pareció la clase? Ayúdanos a mejorar.</p>");
        toClient.println("        <a href='RateClassServlet'><button class='secondary' style='width:100%'>Valorar Clases</button></a>");
        toClient.println("    </div>");

        toClient.println("</div>");

        // Botón de salida
        toClient.println("<div style='text-align: center; margin-top: 20px;'>");
        toClient.println("    <a href='LogoutServlet'><button class='secondary' type='button'>Cerrar Sesión</button></a>");
        toClient.println("</div>");

        toClient.println(Utils.footer("Mi Panel"));
        
        toClient.close();
    }

    // El método doPost simplemente llama al doGet
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
}