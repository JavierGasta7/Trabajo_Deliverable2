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

    public static Vector<String[]> getRoster(Connection connection, int classId) {
        Vector<String[]> vec = new Vector<String[]>();
        String sql = "SELECT u.full_name, u.email, u.phone, b.booked_at " +
                     "FROM bookings b, users u " +
                     "WHERE b.member_id = u.id AND b.class_id = ? AND b.status = 'confirmed' " +
                     "ORDER BY u.full_name";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, classId);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                vec.addElement(new String[]{
                    result.getString("full_name"),
                    result.getString("email"),
                    result.getString("phone"),
                    result.getString("booked_at")
                });
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vec;
    }

    public static int[] getStatsForMember(Connection connection, int memberId) {
        int confirmed = 0, cancelled = 0;
        try {
            PreparedStatement p1 = connection.prepareStatement(
                "SELECT COUNT(*) AS n FROM bookings WHERE member_id = ? AND status = 'confirmed'");
            p1.setInt(1, memberId);
            ResultSet r1 = p1.executeQuery();
            if (r1.next()) confirmed = r1.getInt("n");
            r1.close(); p1.close();

            PreparedStatement p2 = connection.prepareStatement(
                "SELECT COUNT(*) AS n FROM bookings WHERE member_id = ? AND status = 'cancelled'");
            p2.setInt(1, memberId);
            ResultSet r2 = p2.executeQuery();
            if (r2.next()) cancelled = r2.getInt("n");
            r2.close(); p2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{confirmed, cancelled};
    }

    public static String getFavoriteActivity(Connection connection, int memberId) {
        String sql = "SELECT c.activity_type, COUNT(*) AS cnt " +
                     "FROM bookings b, classes c " +
                     "WHERE b.class_id = c.id AND b.member_id = ? AND b.status = 'confirmed' " +
                     "GROUP BY c.activity_type " +
                     "ORDER BY cnt DESC";
        String fav = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) fav = result.getString("activity_type");
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fav;
    }

    public static Vector<BookingData> getRateableClasses(Connection connection, int memberId) {
        Vector<BookingData> vec = new Vector<BookingData>();
        String sql = "SELECT b.id, b.member_id, b.class_id, b.status, b.booked_at, " +
                     "c.name, c.activity_type, c.class_date, c.start_time, " +
                     "u.full_name AS instructor_name " +
                     "FROM bookings b, classes c, users u " +
                     "WHERE b.class_id = c.id AND c.instructor_id = u.id " +
                     "AND b.member_id = ? AND b.status = 'confirmed' " +
                     "AND c.class_date < Date() " +
                     "AND b.class_id NOT IN (SELECT class_id FROM ratings WHERE member_id = ?) " +
                     "ORDER BY c.class_date DESC, c.start_time DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, memberId);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                vec.addElement(new BookingData(
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
                ));
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getRateableClasses: " + sql + " Exception: " + e);
        }
        return vec;
    }

    public static boolean canMemberRate(Connection connection, int memberId, int classId) {
        String sql = "SELECT b.id FROM bookings b, classes c " +
                     "WHERE b.class_id = c.id AND b.member_id = ? AND b.class_id = ? " +
                     "AND b.status = 'confirmed' AND c.class_date < Date()";
        boolean can = false;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, classId);
            ResultSet r = pstmt.executeQuery();
            if (r.next()) can = true;
            r.close(); pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return can;
    }

    public static boolean hasRated(Connection connection, int memberId, int classId) {
        String sql = "SELECT id FROM ratings WHERE member_id = ? AND class_id = ?";
        boolean has = false;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, classId);
            ResultSet r = pstmt.executeQuery();
            if (r.next()) has = true;
            r.close(); pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return has;
    }

    public static int insertRating(Connection connection, int memberId, int classId, int stars, String comment) {
        String sql = "INSERT INTO ratings (member_id, class_id, stars, comment, created_at) VALUES (?, ?, ?, ?, Now())";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, classId);
            pstmt.setInt(3, stars);
            pstmt.setString(4, comment == null ? "" : comment);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in insertRating: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static double getAverageStars(Connection connection, int classId) {
        String sql = "SELECT AVG(stars) AS avgStars FROM ratings WHERE class_id = ?";
        double avg = 0.0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, classId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                avg = rs.getDouble("avgStars");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error in getAverageStars: " + sql + " Exception: " + e);
        }
        return avg;
    }

    public static void completeExpiredBookings(Connection connection) {
        String sql = "UPDATE bookings SET status = 'completed' " +
                     "WHERE status = 'confirmed' " +
                     "AND class_id IN (SELECT id FROM classes WHERE class_date < Date())";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int updated = pstmt.executeUpdate();
            pstmt.close();
            if (updated > 0) {
                System.out.println("completeExpiredBookings: " + updated + " bookings marked as completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in completeExpiredBookings: " + sql + " Exception: " + e);
        }
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
