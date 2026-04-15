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
        res.sendRedirect("login.html");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
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

            toClient.println(Utils.header("Welcome"));
            toClient.println("<div class='card'>");
            toClient.println("<div class='success'>Sign in successful</div>");
            toClient.println("<h2>Welcome back, " + user.fullName + "</h2>");
            toClient.println("<p style='text-align:center;'>Role: <b>" + user.role + "</b></p>");
            toClient.println("<p style='text-align:center;'>Membership: <b>" + user.membershipStatus + "</b></p>");
            toClient.println("<a href='LogoutServlet'><button class='primary' type='button'>Sign Out</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Welcome"));
        } else {
            toClient.println(Utils.header("Sign In"));
            toClient.println("<div class='card'>");
            toClient.println("<div class='error'>Invalid email or password</div>");
            toClient.println("<a href='login.html'><button class='primary' type='button'>Back to Sign In</button></a>");
            toClient.println("</div>");
            toClient.println(Utils.footer("Sign In"));
        }
        toClient.close();
    }
}
