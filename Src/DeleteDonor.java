import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class DeleteDonor {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JLabel nameLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DeleteDonor swingControlDemo = new DeleteDonor();
            swingControlDemo.showButtonDemo();
        });
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Delete Donor Details");
        mainFrame.setSize(700, 400);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(Color.RED);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        headerLabel = new JLabel("Blood Bank Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 27));
        headerLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("Enter donor name to delete.", JLabel.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.DARK_GRAY);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBackground(Color.RED);

        mainFrame.add(headerLabel, BorderLayout.NORTH);
        mainFrame.add(controlPanel, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.setLocationRelativeTo(null);
    }

    public void showButtonDemo() {
        prepareGUI();

        nameLabel = new JLabel("Enter Donor Name:");
        JTextField tfName = new JTextField(20);
        JButton deleteButton = new JButton("DELETE");

        deleteButton.addActionListener(e -> {
            String donorName = normalizeName(tfName.getText().trim()); // Gap 4: Normalize name for consistent case
            if (donorName.isEmpty()) {
                statusLabel.setText("Error: Donor Name is required!"); // Gap 3: Detailed feedback
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to delete donor: " + donorName + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION); // Gap 3: Additional feedback
            if (confirm != JOptionPane.YES_OPTION) {
                statusLabel.setText("Operation cancelled: Deletion aborted."); // Gap 3: Detailed feedback
                return;
            }

            try (
                DBConnection db = new DBConnection();
                Connection conn = db.mkDataBase()
            ) {
                // Begin transaction to ensure consistency
                conn.setAutoCommit(false);
                try (
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM bloodbank WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching
                ) {
                    pst.setString(1, donorName);
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        // Log the deletion
                        db.logAudit("DELETE", donorName); // Gap 6: Log the DELETE operation

                        // Renumber the IDs
                        renumberIds(conn);

                        statusLabel.setText("Success: Donor " + donorName + " deleted and IDs renumbered!"); // Gap 3: Detailed feedback
                        JOptionPane.showMessageDialog(mainFrame, "Donor deleted: " + donorName); // Gap 3: Additional feedback
                        mainFrame.dispose();
                    } else {
                        statusLabel.setText("Error: Donor not found: " + donorName); // Gap 3: Detailed feedback
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Database Error: " + ex.getMessage()); // Gap 3: Detailed feedback
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        inputPanel.add(nameLabel);
        inputPanel.add(tfName);
        inputPanel.add(deleteButton);

        controlPanel.add(inputPanel);
        mainFrame.setVisible(true);
    }

    // Method to renumber IDs after deletion
    private void renumberIds(Connection conn) throws SQLException {
        // Step 1: Create a temporary table with renumbered IDs
        try (Statement stmt = conn.createStatement()) {
            // Disable foreign key checks temporarily (if any)
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            // Create a temporary table with the same structure
            stmt.execute("CREATE TEMPORARY TABLE temp_bloodbank AS SELECT * FROM bloodbank WHERE 1=0");

            // Insert data into temporary table with new IDs
            stmt.execute("INSERT INTO temp_bloodbank (name, bloodgroup, quantity, last_donation_date) " +
                         "SELECT name, bloodgroup, quantity, last_donation_date FROM bloodbank ORDER BY id");

            // Drop the original table
            stmt.execute("TRUNCATE TABLE bloodbank");

            // Insert data back with new auto-incremented IDs
            stmt.execute("INSERT INTO bloodbank (name, bloodgroup, quantity, last_donation_date) " +
                         "SELECT name, bloodgroup, quantity, last_donation_date FROM temp_bloodbank ORDER BY id");

            // Drop the temporary table
            stmt.execute("DROP TEMPORARY TABLE temp_bloodbank");

            // Reset the auto-increment counter
            try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM bloodbank")) {
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        int rowCount = rs.getInt(1);
                        stmt.execute("ALTER TABLE bloodbank AUTO_INCREMENT = " + (rowCount + 1));
                    }
                }
            }

            // Re-enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    // Gap 4: Normalize names for consistent case
    private String normalizeName(String name) {
        if (name.isEmpty()) return name;
        String[] parts = name.trim().split("\\s+");
        StringBuilder normalized = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                normalized.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return normalized.toString().trim();
    }
}