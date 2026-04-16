import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EquipmentData {
    int    equipmentId;
    String name;
    String type;
    String room;
    String status;
    String purchasedAt;
    String lastMaintenance;
    String notes;

    EquipmentData(int equipmentId, String name, String type, String room, String status,
                  String purchasedAt, String lastMaintenance, String notes) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.type = type;
        this.room = room;
        this.status = status;
        this.purchasedAt = purchasedAt;
        this.lastMaintenance = lastMaintenance;
        this.notes = notes;
    }

    public static Vector<EquipmentData> getAll(Connection connection, String filterType, String filterStatus) {
        Vector<EquipmentData> vec = new Vector<EquipmentData>();
        String sql = "SELECT id, name, type, room, status, purchased_at, last_maintenance, notes FROM equipment WHERE 1=1";
        if (filterType != null && !filterType.equals("")) {
            sql += " AND type = ?";
        }
        if (filterStatus != null && !filterStatus.equals("")) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY name";
        System.out.println("getAll equipment: " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int idx = 1;
            if (filterType != null && !filterType.equals("")) {
                pstmt.setString(idx++, filterType);
            }
            if (filterStatus != null && !filterStatus.equals("")) {
                pstmt.setString(idx++, filterStatus);
            }
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                EquipmentData e = new EquipmentData(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("type"),
                    result.getString("room"),
                    result.getString("status"),
                    result.getString("purchased_at"),
                    result.getString("last_maintenance"),
                    result.getString("notes")
                );
                vec.addElement(e);
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getAll equipment: " + sql + " Exception: " + e);
        }
        return vec;
    }

    public static EquipmentData getById(Connection connection, int id) {
        String sql = "SELECT id, name, type, room, status, purchased_at, last_maintenance, notes FROM equipment WHERE id = ?";
        EquipmentData e = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                e = new EquipmentData(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("type"),
                    result.getString("room"),
                    result.getString("status"),
                    result.getString("purchased_at"),
                    result.getString("last_maintenance"),
                    result.getString("notes")
                );
            }
            result.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error in getById equipment: " + sql + " Exception: " + ex);
        }
        return e;
    }

    public static int insert(Connection connection, String name, String type, String room,
                             String status, String purchasedAt, String lastMaintenance, String notes) {
        String sql = "INSERT INTO equipment (name, type, room, status, purchased_at, last_maintenance, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setString(3, room);
            pstmt.setString(4, status);
            pstmt.setString(5, purchasedAt);
            pstmt.setString(6, lastMaintenance);
            pstmt.setString(7, notes);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in insert equipment: " + sql + " Exception: " + e);
        }
        return n;
    }

    public static int update(Connection connection, int id, String name, String type, String room,
                             String status, String lastMaintenance, String notes) {
        String sql = "UPDATE equipment SET name=?, type=?, room=?, status=?, last_maintenance=?, notes=? WHERE id=?";
        int n = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setString(3, room);
            pstmt.setString(4, status);
            pstmt.setString(5, lastMaintenance);
            pstmt.setString(6, notes);
            pstmt.setInt(7, id);
            n = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in update equipment: " + sql + " Exception: " + e);
        }
        return n;
    }
}
