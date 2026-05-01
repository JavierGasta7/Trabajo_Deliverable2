import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class SignUpServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        out.println(Utils.header("Crear cuenta", null));
        out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
        out.println("<div class='title'>Crear cuenta</div>");
        out.println("<div class='subtitle'>Reg\u00edstrate para acceder a GymTrack</div>");
        out.println("<form method='post' action='SignUpServlet' onsubmit='return validarSignUp(this);'>");
        out.println("<label>Nombre completo</label>");
        out.println("<input type='text' name='fullName' required>");
        out.println("<label>Correo electr\u00f3nico</label>");
        out.println("<input type='email' name='email' placeholder='ejemplo@university.edu' required>");
        out.println("<label>Fecha de nacimiento</label>");
        out.println("<input type='date' name='dateOfBirth' required>");
        out.println("<label>Tel\u00e9fono</label>");
        out.println("<input type='text' name='phone' placeholder='600123456' required>");
        out.println("<label>Contrase\u00f1a</label>");
        out.println("<input type='password' name='password' id='pwd1' required oninput='medidorPassword(this,\"pwdMeter\")'>");
        out.println("<div id='pwdMeter' style='margin:4px 0 8px;'></div>");
        out.println("<label>Repetir contrase\u00f1a</label>");
        out.println("<input type='password' name='password2' id='pwd2' required>");
        out.println("<label style='font-size:12px; font-weight:normal;'>");
        out.println("<input type='checkbox' onclick=\"mostrarPassword('pwd1');mostrarPassword('pwd2');\"> Mostrar contrase\u00f1a");
        out.println("</label>");
        out.println("<input type='submit' value='Crear cuenta'>");
        out.println("</form>");
        out.println("<div style='margin-top:15px; text-align:center;'>");
        out.println("<a href='index.html'>\u00bfYa tienes cuenta? Inicia sesi\u00f3n</a>");
        out.println("</div>");
        out.println("</div>");
        out.println(Utils.footer("Sign Up"));
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String fullName    = req.getParameter("fullName");
        String email       = req.getParameter("email");
        String dateOfBirth = req.getParameter("dateOfBirth");
        String phone       = req.getParameter("phone");
        String password    = req.getParameter("password");
        String password2   = req.getParameter("password2");

        String error = null;
        if (fullName == null || fullName.trim().isEmpty()
            || email == null || email.trim().isEmpty()
            || dateOfBirth == null || dateOfBirth.trim().isEmpty()
            || phone == null || phone.trim().isEmpty()
            || password == null || password.isEmpty()) {
            error = "Todos los campos son obligatorios.";
        } else if (!password.equals(password2)) {
            error = "Las contrase\u00f1as no coinciden.";
        } else if (password.length() < 6) {
            error = "La contrase\u00f1a debe tener al menos 6 caracteres.";
        } else if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error = "El correo no tiene un formato v\u00e1lido.";
        } else if (UserData.emailExists(connection, email)) {
            error = "Ya existe una cuenta con ese correo.";
        }

        if (error != null) {
            out.println(Utils.header("Error al registrarse", null));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='error'>" + error + "</div>");
            out.println("<a href='SignUpServlet'><button class='primary' type='button'>Volver</button></a>");
            out.println("</div>");
            out.println(Utils.footer("Error"));
            out.close();
            return;
        }

        int n = UserData.createUser(connection, fullName.trim(), email.trim(), password,
                                    dateOfBirth.trim(), phone.trim());
        if (n > 0) {
            out.println(Utils.header("Cuenta creada", null));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='success'>Cuenta creada correctamente. Ya puedes iniciar sesi\u00f3n.</div>");
            out.println("<a href='index.html'><button class='primary' type='button'>Ir al login</button></a>");
            out.println("</div>");
            out.println(Utils.footer("OK"));
        } else {
            out.println(Utils.header("Error", null));
            out.println("<div class='card' style='max-width:480px; margin:40px auto;'>");
            out.println("<div class='error'>No se pudo crear la cuenta. Int\u00e9ntalo de nuevo.</div>");
            out.println("<a href='SignUpServlet'><button class='primary' type='button'>Volver</button></a>");
            out.println("</div>");
            out.println(Utils.footer("Error"));
        }
        out.close();
    }
}
