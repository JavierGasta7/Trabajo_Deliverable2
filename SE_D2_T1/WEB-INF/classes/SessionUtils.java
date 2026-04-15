import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Clase de ayuda para que los compis comprueben la sesion al inicio de
// sus Servlets. Si no hay sesion activa, redirige a login.html.
//
// Uso (al inicio de doGet/doPost de cualquier Servlet protegido):
//
//   if (!SessionUtils.checkSession(req, res)) return;
//   int    userId    = (int)    req.getSession().getAttribute("userId");
//   String userRole  = (String) req.getSession().getAttribute("userRole");
//   String userName  = (String) req.getSession().getAttribute("userName");
//   String userEmail = (String) req.getSession().getAttribute("userEmail");
//
public class SessionUtils {

    private SessionUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean checkSession(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return false;
        }
        return true;
    }
}
