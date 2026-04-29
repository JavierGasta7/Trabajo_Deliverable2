import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;

@SuppressWarnings("serial")
public class BrowseClassesServlet extends HttpServlet {
    Connection connection;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connection = ConnectionUtils.getConnection(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionUtils.checkSession(req, res)) return;

        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();

        String type = req.getParameter("type");

        // Devuelve los nombres para los desplegables
        if ("filters".equals(type)) {
            Vector<String> classNames = ClassData.getDistinctClassNames(connection);
            Vector<String> instructorNames = ClassData.getDistinctInstructorNames(connection);
            out.print("{\"classNames\":[");
            for (int i = 0; i < classNames.size(); i++) {
                if (i > 0) out.print(",");
                out.print("\"" + classNames.elementAt(i) + "\"");
            }
            out.print("],\"instructorNames\":[");
            for (int i = 0; i < instructorNames.size(); i++) {
                if (i > 0) out.print(",");
                out.print("\"" + instructorNames.elementAt(i) + "\"");
            }
            out.print("]}");
            out.close();
            return;
        }

        // Devuelve las clases (con filtros opcionales)
        String filterClass = req.getParameter("className");
        String filterInstructor = req.getParameter("instructorName");
        Vector<ClassData> classList = ClassData.getAvailableClasses(connection, filterClass, filterInstructor);

        out.print("[");
        for (int i = 0; i < classList.size(); i++) {
            ClassData c = classList.elementAt(i);
            if (i > 0) out.print(",");
            out.print("{");
            out.print("\"classId\":" + c.classId + ",");
            out.print("\"name\":\"" + c.name + "\",");
            out.print("\"activityType\":\"" + c.activityType + "\",");
            out.print("\"instructorName\":\"" + c.instructorName + "\",");
            out.print("\"room\":\"" + c.room + "\",");
            out.print("\"classDate\":\"" + Utils.formatDate(c.classDate) + "\",");
            out.print("\"startTime\":\"" + Utils.formatTime(c.startTime) + "\",");
            out.print("\"maxCapacity\":" + c.maxCapacity + ",");
            out.print("\"bookedCount\":" + c.bookedCount);
            out.print("}");
        }
        out.print("]");
        out.close();
    }
}