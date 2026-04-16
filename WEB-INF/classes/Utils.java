import javax.servlet.http.HttpSession;

public class Utils {
    public static String header(String title, HttpSession session) {
        // Determinamos a dónde enviar al usuario según su rol
        String homeLink = "index.html"; // Por defecto al index
        
        if (session != null && session.getAttribute("userRole") != null) {
            String role = (String) session.getAttribute("userRole");
            
            if (role.equalsIgnoreCase("admin")) {
                homeLink = "AdminDashboardServlet";
            } else if (role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("manager")) {
                homeLink = "InstructorDashboardServlet";
            } else if (role.equalsIgnoreCase("member")) {
                homeLink = "MemberDashboardServlet";
            }
        }

        StringBuilder str = new StringBuilder();
        str.append("<!DOCTYPE HTML><html><head>");
        str.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
        str.append("<title>" + title + "</title>");
        str.append("<link rel='stylesheet' href='style.css'></head><body>");
        
        str.append("<div class='menu'>");
        str.append("<span class='brand'>GYMTRACK</span>");
        

        str.append("<a href='" + homeLink + "'>Inicio</a>"); 
        str.append("<a href='LogoutServlet'>Cerrar Sesi\u00f3n</a>");
        str.append("</div>");
        
        str.append("<div class='subheader'>" + title + "</div>");
        return str.toString();
    }

    // Mantén el método footer igual
    public static String footer(String title) {
        return "</body></html>";
    }

    // Formatea fechas de Access: "yyyy-MM-dd ..." -> "yyyy-MM-dd"
    public static String formatDate(String s) {
        if (s == null) return "";
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
    }

    // Formatea horas de Access. Valores t\u00edpicos:
    //   "1899-12-30 10:00:00.0"  -> "10:00"
    //   "10:00:00"                -> "10:00"
    //   "10:00"                   -> "10:00"
    public static String formatTime(String s) {
        if (s == null) return "";
        int colonIdx = s.indexOf(':');
        if (colonIdx <= 0) return s;
        int start = colonIdx - 2;
        if (start < 0) start = 0;
        int end = start + 5;
        if (end > s.length()) end = s.length();
        return s.substring(start, end);
    }
}