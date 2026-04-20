import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class PersonalStatsServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        if (!SessionUtils.checkSession(req, res)) return;

        BookingData.completeExpiredBookings(connection);

        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        int[] stats = BookingData.getStatsForMember(connection, userId);
        int confirmed = stats[0];
        int cancelled = stats[1];
        int total = confirmed + cancelled;
        int rate = (total > 0) ? (confirmed * 100 / total) : 0;
        String favorite = BookingData.getFavoriteActivity(connection, userId);

        String medal, medalColor, medalEmoji;
        if (confirmed >= 25)      { medal = "Oro";    medalColor = "#f59e0b"; medalEmoji = "\uD83E\uDD47"; }
        else if (confirmed >= 10) { medal = "Plata";  medalColor = "#9ca3af"; medalEmoji = "\uD83E\uDD48"; }
        else if (confirmed >= 3)  { medal = "Bronce"; medalColor = "#b45309"; medalEmoji = "\uD83E\uDD49"; }
        else                      { medal = "Sin medalla todav\u00eda"; medalColor = "#6b7280"; medalEmoji = ""; }

        String rateColor = (rate >= 80) ? "#10b981" : (rate >= 50 ? "#f59e0b" : "#ef4444");

        PrintWriter out = res.getWriter();
        out.println(Utils.header("Mis estad\u00edsticas", session));

        out.println("<div class='card' style='max-width:900px; margin:20px auto;'>");
        out.println("<div class='title'>Mis estad\u00edsticas</div>");
        out.println("<div class='subtitle'>Resumen de actividad de " + safe(userName) + "</div>");

        out.println("<div style='display:flex; gap:15px; justify-content:center; margin:25px 0; flex-wrap:wrap;'>");
        out.println(summaryCard("Confirmadas", String.valueOf(confirmed), "#2563eb"));
        out.println(summaryCard("Canceladas", String.valueOf(cancelled), "#ef4444"));
        out.println(summaryCard("Total reservas", String.valueOf(total), "#6b7280"));
        out.println("</div>");

        out.println("<h3 style='margin-top:20px; color:#1f2937;'>Tasa de asistencia</h3>");
        out.println("<div style='background:#e9ecef; border-radius:8px; height:24px; overflow:hidden; margin:10px 0;'>");
        out.println("<div style='width:" + rate + "%; background:" + rateColor +
                    "; height:100%; color:white; text-align:center; line-height:24px; font-weight:bold; font-size:13px;'>" +
                    rate + "%</div>");
        out.println("</div>");
        out.println("<p style='font-size:13px; color:#6b7280;'>" +
                    (total == 0 ? "A\u00fan no tienes reservas." :
                     "Has confirmado " + confirmed + " de " + total + " reservas.") + "</p>");

        out.println("<div style='display:flex; gap:20px; margin-top:30px; flex-wrap:wrap;'>");

        out.println("<div style='flex:1; min-width:250px; padding:20px; border:1px solid #e5e7eb; border-radius:8px;'>");
        out.println("<div style='font-size:13px; color:#6b7280; text-transform:uppercase; margin-bottom:8px;'>Actividad favorita</div>");
        out.println("<div style='font-size:22px; font-weight:bold; color:#2563eb;'>" +
                    (favorite == null ? "Sin datos" : safe(favorite)) + "</div>");
        out.println("<div style='font-size:12px; color:#6b7280; margin-top:5px;'>La que m\u00e1s has reservado</div>");
        out.println("</div>");

        out.println("<div style='flex:1; min-width:250px; padding:20px; border:2px solid " + medalColor +
                    "; border-radius:8px; background:" + medalColor + "10;'>");
        out.println("<div style='font-size:13px; color:#6b7280; text-transform:uppercase; margin-bottom:8px;'>Medalla de fidelidad</div>");
        out.println("<div style='font-size:22px; font-weight:bold; color:" + medalColor + ";'>" + medalEmoji + " " + medal + "</div>");
        out.println("<div style='font-size:12px; color:#6b7280; margin-top:5px;'>Bronce 3 \u00b7 Plata 10 \u00b7 Oro 25 reservas</div>");
        out.println("</div>");

        out.println("</div>");

        out.println("<div style='text-align:center; margin-top:30px;'>");
        out.println("<a href='BrowseClassesServlet'><button class='primary' type='button' style='width:auto; padding:10px 20px;'>Reservar m\u00e1s clases</button></a>");
        out.println("<a href='MemberDashboardServlet'><button class='secondary' type='button' style='width:auto; padding:10px 20px; margin-left:10px;'>Volver al panel</button></a>");
        out.println("</div>");

        out.println("</div>");
        out.println(Utils.footer("Personal Stats"));
        out.close();
    }

    private String summaryCard(String label, String value, String color) {
        return "<div style='flex:1; max-width:200px; min-width:140px; text-align:center; padding:20px; " +
               "border-radius:8px; background:" + color + "; color:white;'>" +
               "<div style='font-size:32px; font-weight:bold;'>" + value + "</div>" +
               "<div style='font-size:13px;'>" + label + "</div>" +
               "</div>";
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
