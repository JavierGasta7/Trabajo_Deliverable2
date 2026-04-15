import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingData {
    int    bookingId;
    int    memberId;
    int    classId;
    String className;
    String activityType;
    String classDate;
    String startTime;
    String instructorName;
    String status;
    String bookedAt;

    BookingData(int bookingId, int memberId, int classId, String className,
                String activityType, String classDate, String startTime,
                String instructorName, String status, String bookedAt) {
        this.bookingId = bookingId;
        this.memberId = memberId;
        this.classId = classId;
        this.className = className;
        this.activityType = activityType;
        this.classDate = classDate;
        this.startTime = startTime;
        this.instructorName = instructorName;
        this.status = status;
        this.bookedAt = bookedAt;
    }

    public static Vector<BookingData> getHistory(Connection connection, int memberId) {
        Vector<BookingData> vec = new Vector<BookingData>();
        String sql = "SELECT b.id, b.member_id, b.class_id, b.status, b.booked_at, " +
                     "c.name, c.activity_type, c.class_date, c.start_time, " +
                     "u.full_name AS instructor_name " +
                     "FROM bookings b, classes c, users u " +
                     "WHERE b.class_id = c.id AND c.instructor_id = u.id " +
                     "AND b.member_id = ? " +
                     "ORDER BY c.class_date DESC, c.start_time DESC";
        System.out.println("getHistory: " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                BookingData b = new BookingData(
                    result.getInt("id"),
                    result.getInt("member_id"),
                    result.getInt("class_id"),
                    result.getString("name"),
                    result.getString("activity_type"),
                    result.getString("class_date"),
                    result.getString("start_time"),
                    result.getString("instructor_name"),
                    result.getString("status"),
                    result.getString("booked_at")
                );
                vec.addElement(b);
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getHistory: " + sql + " Exception: " + e);
        }
        return vec;
    }

    public static BookingData getBooking(Connection connection, int bookingId) {
        String sql = "SELECT b.id, b.member_id, b.class_id, b.status, b.booked_at, " +
                     "c.name, c.activity_type, c.class_date, c.start_time, " +
                     "u.full_name AS instructor_name " +
                     "FROM bookings b, classes c, users u " +
                     "WHERE b.class_id = c.id AND c.instructor_id = u.id AND b.id = ?";
        System.out.println("getBooking: " + sql);
        BookingData b = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                b = new BookingData(
                    result.getInt("id"),
                    result.getInt("member_id"),
                    result.getInt("class_id"),
                    result.getString("name"),
                    result.getString("activity_type"),
                    result.getString("class_date"),
                    result.getString("start_time"),
                    result.getString("instructor_name"),
                    result.getString("status"),
                    result.getString("booked_at")
                );
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getBooking: " + sql + " Exception: " + e);
        }
        return b;
    }

    public static boolean isAlreadyBooked(Connection connection, int memberId, int classId) {
        String sql = "SELECT id FROM bookings WHERE member_id = ? AND class_id = ? AND status = 'confirmed'";
        boolean found = false;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, classId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                found = true;
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in isAlreadyBooked: " + sql + " Exception: " + e);
        }
        return found;
    }

    public static int insertBooking(Connection connection, int memberId, int classId) {
        String sql = "INSERT INTO bookings (member_id, class_id, status, booked_at) VALUES (?, ?, 'confirmed', Now())";
        System.out.println("insertBooking: " + sql);
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, classId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in insertBooking: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static int cancelBooking(Connection connection, int bookingId) {
        String sql = "UPDATE bookings SET status = 'cancelled' WHERE id = ?";
        System.out.println("cancelBooking: " + sql);
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in cancelBooking: " + sql + " Exception: " + e);
        }
        return n;
    }
}
