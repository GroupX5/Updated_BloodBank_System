import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection implements AutoCloseable {
    private static final String URL = "jdbc:mysql://localhost:3306/bloodbank";
    private static final String USER = "root";
    private static final String PASS = "Yoni@13018002";
    private Connection connection;

    public Connection mkDataBase() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
            return connection;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw new SQLException("Database driver not found", ex);
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Added for Gap 2: Check if donor already exists (duplicate check)
    public boolean donorExists(String name) throws SQLException {
        try (
            Connection conn = mkDataBase();
            PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM bloodbank WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching
        ) {
            pst.setString(1, name);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    // Added for Gap 5: Calculate total blood stock by blood group
    public int getBloodStock(String bloodGroup) throws SQLException {
        try (
            Connection conn = mkDataBase();
            PreparedStatement pst = conn.prepareStatement("SELECT SUM(quantity) AS total FROM bloodbank WHERE bloodgroup = ?")
        ) {
            pst.setString(1, bloodGroup);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        }
    }

    // Added for Gap 6: Log operations to audit_log table
    public void logAudit(String operation, String donorName) throws SQLException {
        try (
            Connection conn = mkDataBase();
            PreparedStatement pst = conn.prepareStatement("INSERT INTO audit_log (operation, donor_name) VALUES (?, ?)")
        ) {
            pst.setString(1, operation);
            pst.setString(2, donorName);
            pst.executeUpdate();
        }
    }

    // Added for Gap 7: Check donor eligibility based on last donation date
    public boolean isDonorEligible(String name) throws SQLException {
        try (
            Connection conn = mkDataBase();
            PreparedStatement pst = conn.prepareStatement("SELECT DATEDIFF(CURRENT_DATE, last_donation_date) AS days_since FROM bloodbank WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching
        ) {
            pst.setString(1, name);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getObject("days_since") != null) {
                    int daysSince = rs.getInt("days_since");
                    return daysSince >= 56;
                }
                return true;
            }
        }
    }
}