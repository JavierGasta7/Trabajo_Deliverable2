import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassData {
    int    classId;
    String name;
    String activityType;
    String room;
    String classDate;
    String startTime;
    int    durationMin;
    int    maxCapacity;
    int    instructorId;
    String instructorName;
    int    bookedCount;

    ClassData(int classId, String name, String activityType, String room,
              String classDate, String startTime, int durationMin, int maxCapacity,
              int instructorId, String instructorName, int bookedCount) {
        this.classId = classId;
        this.name = name;
        this.activityType = activityType;
        this.room = room;
        this.classDate = classDate;
        this.startTime = startTime;
        this.durationMin = durationMin;
        this.maxCapacity = maxCapacity;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
        this.bookedCount = bookedCount;
    }

    public static Vector<ClassData> getAvailableClasses(Connection connection,
            String filterActivity, String filterInstructor) {
        Vector<ClassData> vec = new Vector<ClassData>();
        String sql = "SELECT c.id, c.name, c.activity_type, c.room, c.class_date, c.start_time, " +
                     "c.duration_min, c.max_capacity, c.instructor_id, u.full_name AS instructor_name " +
                     "FROM classes c, users u " +
                     "WHERE c.instructor_id = u.id";
		if (filterActivity != null && !filterActivity.equals("")) {
            sql += " AND c.name = '" + filterActivity + "'";
        }
        if (filterInstructor != null && !filterInstructor.equals("")) {
            sql += " AND u.full_name = '" + filterInstructor + "'";
        }
        sql += " ORDER BY c.class_date, c.start_time";
        System.out.println("getAvailableClasses: " + sql);

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                int classId = result.getInt("id");
                int booked = countBookings(connection, classId);
                ClassData c = new ClassData(
                    classId,
                    result.getString("name"),
                    result.getString("activity_type"),
                    result.getString("room"),
                    result.getString("class_date"),
                    result.getString("start_time"),
                    result.getInt("duration_min"),
                    result.getInt("max_capacity"),
                    result.getInt("instructor_id"),
                    result.getString("instructor_name"),
                    booked
                );
                vec.addElement(c);
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getAvailableClasses: " + sql + " Exception: " + e);
        }
        return vec;
    }

    public static ClassData getClass(Connection connection, int classId) {
        String sql = "SELECT c.id, c.name, c.activity_type, c.room, c.class_date, c.start_time, " +
                     "c.duration_min, c.max_capacity, c.instructor_id, u.full_name AS instructor_name " +
                     "FROM classes c, users u " +
                     "WHERE c.instructor_id = u.id AND c.id = ?";
        System.out.println("getClass: " + sql);
        ClassData c = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, classId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                int booked = countBookings(connection, classId);
                c = new ClassData(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("activity_type"),
                    result.getString("room"),
                    result.getString("class_date"),
                    result.getString("start_time"),
                    result.getInt("duration_min"),
                    result.getInt("max_capacity"),
                    result.getInt("instructor_id"),
                    result.getString("instructor_name"),
                    booked
                );
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getClass: " + sql + " Exception: " + e);
        }
        return c;
    }

    public static Vector<ClassData> getClassesByInstructor(Connection connection, int instructorId) {
        Vector<ClassData> vec = new Vector<ClassData>();
        String sql = "SELECT c.id, c.name, c.activity_type, c.room, c.class_date, c.start_time, " +
                     "c.duration_min, c.max_capacity, c.instructor_id, u.full_name AS instructor_name " +
                     "FROM classes c, users u " +
                     "WHERE c.instructor_id = u.id AND c.instructor_id = ? " +
                     "ORDER BY c.class_date DESC, c.start_time";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, instructorId);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                int classId = result.getInt("id");
                int booked = countBookings(connection, classId);
                vec.addElement(new ClassData(
                    classId,
                    result.getString("name"),
                    result.getString("activity_type"),
                    result.getString("room"),
                    result.getString("class_date"),
                    result.getString("start_time"),
                    result.getInt("duration_min"),
                    result.getInt("max_capacity"),
                    result.getInt("instructor_id"),
                    result.getString("instructor_name"),
                    booked
                ));
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vec;
    }

    public static int insertClass(Connection connection, String name, String activityType,
                                  String room, String classDate, String startTime,
                                  int durationMin, int maxCapacity, int instructorId) {
        String sql = "INSERT INTO classes (name, activity_type, room, class_date, start_time, duration_min, max_capacity, instructor_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, activityType);
            pstmt.setString(3, room);
            pstmt.setString(4, classDate);
            pstmt.setString(5, startTime);
            pstmt.setInt(6, durationMin);
            pstmt.setInt(7, maxCapacity);
            pstmt.setInt(8, instructorId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in insertClass: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static int countBookings(Connection connection, int classId) {
        String sql = "SELECT COUNT(*) AS n FROM bookings WHERE class_id = ? AND status = 'confirmed'";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, classId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                n = result.getInt("n");
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in countBookings: " + sql + " Exception: " + e);
        }
        return n;
    }
	
	public static Vector<String> getDistinctClassNames(Connection connection) {
        Vector<String> vec = new Vector<String>();
        String sql = "SELECT DISTINCT name FROM classes ORDER BY name";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                vec.addElement(result.getString("name"));
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getDistinctClassNames: " + e);
        }
        return vec;
    }

    public static Vector<String> getDistinctInstructorNames(Connection connection) {
        Vector<String> vec = new Vector<String>();
        String sql = "SELECT DISTINCT u.full_name FROM classes c, users u " +
                     "WHERE c.instructor_id = u.id ORDER BY u.full_name";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                vec.addElement(result.getString("full_name"));
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getDistinctInstructorNames: " + e);
        }
        return vec;
    }
}
