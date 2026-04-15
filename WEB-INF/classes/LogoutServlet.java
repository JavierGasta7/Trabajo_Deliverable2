import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // 1. Intentamos obtener la sesión actual (sin crear una nueva)
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            // 2. Borramos todos los datos almacenados (userId, userRole, etc.)
            session.invalidate();
        }

        // 3. Redirigimos al nuevo index.html para que el usuario pueda volver a loguearse
        res.sendRedirect("index.html");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Hacemos lo mismo si llega una petición POST
        doGet(req, res);
    }
}