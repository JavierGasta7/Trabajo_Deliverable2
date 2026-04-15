public class Utils {
    public static String header(String title) {
        StringBuilder str = new StringBuilder();
        str.append("<!DOCTYPE HTML>");
        str.append("<html>");
        str.append("<head><title>" + title + "</title>");
        str.append("<link rel='stylesheet' href='style.css'>");
        str.append("</head>");
        str.append("<body>");
        str.append("<div class='menu'>");
        str.append("<span class='brand'>GYMTRACK</span>");
        str.append("<a href='index.html'>Home</a>");
        str.append("<a href='login.html'>Sign In</a>");
        str.append("<a href='LogoutServlet'>Logout</a>");
        str.append("</div>");
        str.append("<div class='subheader'>" + title + "</div>");
        return str.toString();
    }

    public static String footer(String title) {
        StringBuilder str = new StringBuilder();
        str.append("</body>");
        str.append("</html>");
        return str.toString();
    }
}
