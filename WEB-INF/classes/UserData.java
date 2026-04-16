import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

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

    public static int updateProfile(Connection connection, int userId, String fullName,
                                    String phone, String dateOfBirth) {
        String sql = "UPDATE users SET full_name=?, phone=?, date_of_birth=? WHERE id=?";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, phone);
            pstmt.setString(3, dateOfBirth);
            pstmt.setInt(4, userId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in updateProfile: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static boolean emailExists(Connection connection, String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        boolean found = false;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) found = true;
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }

    public static int createUser(Connection connection, String fullName, String email, String password,
                                 String dateOfBirth, String phone) {
        String sql = "INSERT INTO users (full_name, email, password, role, date_of_birth, phone, membership_status, created_at) " +
                     "VALUES (?, ?, ?, 'member', ?, ?, 'active', Now())";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, dateOfBirth);
            pstmt.setString(5, phone);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in createUser: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static Vector<UserData> getAllUsers(Connection connection, String roleFilter, String statusFilter, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, full_name, email, password, role, membership_status FROM users WHERE 1=1");
        if (roleFilter != null && !roleFilter.isEmpty())   sql.append(" AND role=?");
        if (statusFilter != null && !statusFilter.isEmpty()) sql.append(" AND membership_status=?");
        if (search != null && !search.isEmpty())           sql.append(" AND (full_name LIKE ? OR email LIKE ?)");
        sql.append(" ORDER BY role, full_name");

        Vector<UserData> list = new Vector<UserData>();
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql.toString());
            int i = 1;
            if (roleFilter != null && !roleFilter.isEmpty())   pstmt.setString(i++, roleFilter);
            if (statusFilter != null && !statusFilter.isEmpty()) pstmt.setString(i++, statusFilter);
            if (search != null && !search.isEmpty()) {
                pstmt.setString(i++, "%" + search + "%");
                pstmt.setString(i++, "%" + search + "%");
            }
            ResultSet r = pstmt.executeQuery();
            while (r.next()) {
                list.add(new UserData(
                    r.getInt("id"), r.getString("full_name"), r.getString("email"),
                    r.getString("password"), r.getString("role"), r.getString("membership_status")
                ));
            }
            r.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getAllUsers: " + sql + " Exception: " + e);
        }
        return list;
    }

    public static int updateRole(Connection connection, int userId, String newRole) {
        String sql = "UPDATE users SET role=? WHERE id=?";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in updateRole: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static int updateMembershipStatus(Connection connection, int userId, String status) {
        String sql = "UPDATE users SET membership_status=? WHERE id=?";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in updateMembershipStatus: " + sql + " Exception: " + e);
        }
        return n;
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
