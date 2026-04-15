import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserData {
    int    userId;
    String fullName;
    String email;
    String password;
    String role;
    String membershipStatus;

    UserData(int userId, String fullName, String email, String password, String role, String membershipStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.membershipStatus = membershipStatus;
    }

    public static UserData login(Connection connection, String email, String password) {
        String sql = "SELECT id, full_name, email, password, role, membership_status FROM users";
        sql += " WHERE email=? AND password=?";
        System.out.println("login: " + sql);
        UserData user = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                user = new UserData(
                    result.getInt("id"),
                    result.getString("full_name"),
                    result.getString("email"),
                    result.getString("password"),
                    result.getString("role"),
                    result.getString("membership_status")
                );
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in login: " + sql + " Exception: " + e);
        }
        return user;
    }

    public static UserData getUser(Connection connection, int userId) {
        String sql = "SELECT id, full_name, email, password, role, membership_status FROM users";
        sql += " WHERE id=?";
        System.out.println("getUser: " + sql);
        UserData user = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                user = new UserData(
                    result.getInt("id"),
                    result.getString("full_name"),
                    result.getString("email"),
                    result.getString("password"),
                    result.getString("role"),
                    result.getString("membership_status")
                );
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getUser: " + sql + " Exception: " + e);
        }
        return user;
    }
}
