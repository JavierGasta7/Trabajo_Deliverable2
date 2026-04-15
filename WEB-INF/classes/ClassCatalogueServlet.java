import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class ClassCatalogueServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verificación de Seguridad
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        String userRole = (String) session.getAttribute("userRole");
        // Solo Admin o Manager pueden gestionar el catálogo
        if (!"admin".equalsIgnoreCase(userRole) && !"manager".equalsIgnoreCase(userRole) && !"instructor".equalsIgnoreCase(userRole)) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. IMPORTANTE: Configurar codificación para evitar símbolos raros
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        PrintWriter out = response.getWriter();
        Connection conn = null;

        out.println(Utils.header("Gestión del Catálogo", session));

        try {
            conn = ConnectionUtils.getConnection(getServletConfig());

            // ================= FORMULARIO DE EDICIÓN =================
            if ("edit".equals(action) && idParam != null) {
                int id = Integer.parseInt(idParam);
                String sql = "SELECT * FROM classes WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    out.println("<div class='card' style='max-width:600px; margin: 20px auto;'>");
                    out.println("<div class='title'>Editar Clase</div>");
                    out.println("<form method='post' action='ClassCatalogueServlet'>");
                    out.println("<input type='hidden' name='action' value='edit'>");
                    out.println("<input type='hidden' name='id' value='" + id + "'>");

                    out.println("<label>Nombre de la Actividad:</label><br>");
                    out.println("<input type='text' name='name' value='" + rs.getString("name") + "' style='width:100%; padding:8px; margin:10px 0;'><br>");

                    out.println("<button class='primary' type='submit'>Guardar Cambios</button>");
                    out.println(" <a href='ClassCatalogueServlet' class='secondary' style='text-decoration:none; padding:10px;'>Cancelar</a>");
                    out.println("</form>");
                    out.println("</div>");
                }
                rs.close();
                ps.close();
            }

            // ================= LISTADO DE CLASES =================
            String sql = "SELECT * FROM classes ORDER BY class_date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.println("<div class='subheader'>Catálogo de Actividades</div>");
            out.println("<div class='card'>");

            while (rs.next()) {
                int id = rs.getInt("id");
                out.println("<div style='border-bottom:1px solid #eee; padding:15px; display:flex; justify-content:space-between; align-items:center;'>");
                
                out.println("<div>");
                out.println("<b style='font-size:1.1em;'>" + rs.getString("name") + "</b><br>");
                out.println("<span style='color:#666; font-size:0.9em;'>Fecha: " + rs.getString("class_date") + " | Hora: " + rs.getString("start_time") + " | Sala: " + rs.getString("room") + "</span>");
                out.println("</div>");

                out.println("<div style='display:flex; gap:10px;'>");
                // Botón Editar
                out.println("<a href='ClassCatalogueServlet?action=edit&id=" + id + "'><button class='secondary' style='padding:5px 10px;'>Editar</button></a>");

                // Botón Cancelar (Formulario POST por seguridad)
                out.println("<form method='post' action='ClassCatalogueServlet' style='margin:0;'>");
                out.println("<input type='hidden' name='action' value='cancel'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<button type='submit' style='background:#ff4d4d; color:white; border:none; padding:6px 10px; border-radius:4px; cursor:pointer;' onclick=\"return confirm('¿Seguro que quieres cancelar esta clase?')\">Cancelar</button>");
                out.println("</form>");
                out.println("</div>");

                out.println("</div>");
            }

            out.println("</div>");
            rs.close();
            ps.close();

        } catch (Exception e) {
            out.println("<div class='error'>Error al cargar el catálogo: " + e.getMessage() + "</div>");
        } finally {
            ConnectionUtils.close(conn);
        }

        out.println("<div style='text-align:center; margin-top:20px;'><a href='index.html' class='secondary' style='text-decoration:none;'>Volver al Panel</a></div>");
        out.println(Utils.footer("GymTrack Catalog"));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar encoding también en el POST
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null) {
            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        Connection conn = null;

        try {
            conn = ConnectionUtils.getConnection(getServletConfig());
            int id = Integer.parseInt(idParam);

            if ("cancel".equals(action)) {
                // Cancelar reservas asociadas
                String sql = "UPDATE bookings SET status='cancelled' WHERE class_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
            }

            if ("edit".equals(action)) {
                String name = request.getParameter("name");
                String sql = "UPDATE classes SET name=? WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, id);
                ps.executeUpdate();
                ps.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(conn);
        }

        // Volver al listado tras la operación
        response.sendRedirect("ClassCatalogueServlet");
    }
}