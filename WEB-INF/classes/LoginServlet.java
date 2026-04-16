import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.sendRedirect("index.html");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // 1. IMPORTANTE: Configurar UTF-8 para que el mensaje de error se vea bien
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter toClient = res.getWriter();

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        UserData user = UserData.login(connection, email, password);

        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", new Integer(user.userId));
            session.setAttribute("userRole", user.role);
            session.setAttribute("userEmail", user.email);
            session.setAttribute("userName", user.fullName);
            session.setAttribute("user", user);

            if (user.role.equalsIgnoreCase("admin")) {
                res.sendRedirect("AdminDashboardServlet");
            } 
            else if (user.role.equalsIgnoreCase("instructor") || user.role.equalsIgnoreCase("manager")) {
                res.sendRedirect("InstructorDashboardServlet"); 
            } 
            else {
                res.sendRedirect("MemberDashboardServlet"); 
            }

        } else {
            // 2. SOLUCIÓN AL ERROR: 
            // Como el login falló, NO hay sesión. Pasamos 'null' al header.
            // Utils.header está preparado para recibir null y mandar al index.html
            toClient.println(Utils.header("Error de acceso", null));
            
            toClient.println("<div class='card' style='max-width:400px; margin: 40px auto;'>");
            toClient.println("<div class='title' style='color:#dc3545; text-align:center;'>Acceso Denegado</div>");
            toClient.println("<p style='text-align:center;'>Usuario o contrase\u00f1a incorrectos. Por favor, verifica tus datos.</p>");
            toClient.println("<div style='text-align:center; margin-top:20px;'>");
            toClient.println("    <a href='index.html'><button class='primary' style='width:100%'>Volver al Inicio</button></a>");
            toClient.println("</div>");
            toClient.println("</div>");
            
            toClient.println(Utils.footer("Error"));
        }
        toClient.close();
    }
}