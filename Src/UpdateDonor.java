import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UpdateDonor {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JLabel nameLabel, bloodGroupLabel, quantityLabel;
    private GridLayout experimentLayout = new GridLayout(0, 2);
    private static final Logger LOGGER = Logger.getLogger(UpdateDonor.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UpdateDonor swingControlDemo = new UpdateDonor();
            swingControlDemo.prepareGUI();
            swingControlDemo.showButtonDemo();
        });
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Update Donor Details");
        mainFrame.setSize(700, 400);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(Color.RED);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        headerLabel = new JLabel("Blood Bank Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 27));
        headerLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("Enter details to update a donor.", JLabel.CENTER);
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
        // Ensure GUI is initialized before proceeding
        prepareGUI(); // Fix: Call prepareGUI to initialize controlPanel and other components

        nameLabel = new JLabel("Enter Donor Name");
        JTextField tfName = new JTextField();
        tfName.setPreferredSize(new Dimension(150, 25));

        bloodGroupLabel = new JLabel("Enter Blood Group");
        JTextField tfBloodGroup = new JTextField();
        tfBloodGroup.setPreferredSize(new Dimension(150, 25));

        quantityLabel = new JLabel("Enter Quantity (max 500 mL)"); // Gap 5: Indicate quantity limit in label
        JTextField tfQuantity = new JTextField();
        tfQuantity.setPreferredSize(new Dimension(150, 25));

        JButton okButton = new JButton("UPDATE");
        okButton.addActionListener(e -> {
            String name = normalizeName(tfName.getText().trim()); // Gap 4: Normalize name for consistent case
            String bloodGroup = tfBloodGroup.getText().trim().toUpperCase();
            String quantityStr = tfQuantity.getText().trim();

            LOGGER.info("Starting update process for donor: " + name);

            // Validate inputs
            if (name.isEmpty() || bloodGroup.isEmpty() || quantityStr.isEmpty()) {
                statusLabel.setText("Error: All fields are required!"); // Gap 3: Detailed feedback
                LOGGER.warning("Validation failed: All fields are required");
                return;
            }

            // Gap 1: Validate blood group
            String[] validBloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            boolean isValidBloodGroup = false;
            for (String validGroup : validBloodGroups) {
                if (bloodGroup.equals(validGroup)) {
                    isValidBloodGroup = true;
                    break;
                }
            }
            if (!isValidBloodGroup) {
                statusLabel.setText("Error: Invalid blood group! Use format like A+, B-, etc."); // Gap 3: Detailed feedback
                LOGGER.warning("Validation failed: Invalid blood group - " + bloodGroup);
                return;
            }

            // Gap 5: Validate quantity limits
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0 || quantity > 500) {
                    statusLabel.setText("Error: Quantity must be between 1 and 500 mL!"); // Gap 3: Detailed feedback
                    LOGGER.warning("Validation failed: Invalid quantity - " + quantity);
                    return;
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Quantity must be a valid number!"); // Gap 3: Detailed feedback
                LOGGER.warning("Validation failed: Quantity not a number - " + quantityStr);
                return;
            }

            // Gap 7: Check donor existence and eligibility before update
            try (DBConnection con = new DBConnection()) {
                LOGGER.info("Checking donor existence for: " + name);
                // Check if donor exists
                if (!con.donorExists(name)) {
                    statusLabel.setText("Error: Donor not found: " + name); // Gap 3: Detailed feedback
                    LOGGER.warning("Donor not found: " + name);
                    return;
                }

                // Check eligibility
                LOGGER.info("Checking eligibility for: " + name);
                boolean eligible = true;
                try (
                    PreparedStatement eligibilityStmt = con.mkDataBase().prepareStatement(
                        "SELECT last_donation_date FROM bloodbank WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching
                ) {
                    eligibilityStmt.setString(1, name);
                    try (ResultSet rs = eligibilityStmt.executeQuery()) {
                        if (rs.next()) {
                            Date lastDonation = rs.getDate("last_donation_date");
                            LOGGER.info("Last donation date for " + name + ": " + (lastDonation != null ? lastDonation.toString() : "null"));
                            if (lastDonation != null) {
                                // Use SQL to calculate days difference to avoid Java date issues
                                try (
                                    PreparedStatement daysStmt = con.mkDataBase().prepareStatement(
                                        "SELECT DATEDIFF(CURRENT_DATE, ?) AS days_since")
                                ) {
                                    daysStmt.setDate(1, lastDonation);
                                    try (ResultSet daysRs = daysStmt.executeQuery()) {
                                        if (daysRs.next()) {
                                            int daysSince = daysRs.getInt("days_since");
                                            LOGGER.info("Days since last donation: " + daysSince);
                                            eligible = daysSince >= 56;
                                        }
                                    }
                                }
                            } else {
                                LOGGER.info("No previous donation date for " + name + ", donor is eligible");
                            }
                        } else {
                            LOGGER.warning("No result returned for eligibility check for: " + name);
                        }
                    }
                } catch (SQLException ex) {
                    statusLabel.setText("Error checking eligibility: " + ex.getMessage()); // Gap 3: Detailed feedback
                    LOGGER.log(Level.SEVERE, "Failed to check eligibility for " + name, ex);
                    return;
                }

                if (!eligible) {
                    statusLabel.setText("Error: " + name + " is not eligible (less than 56 days since last donation)."); // Gap 7 & Gap 3: Eligibility check and feedback
                    LOGGER.warning(name + " is not eligible for donation");
                    return;
                }

                // Update donor
                LOGGER.info("Updating donor: " + name);
                try (
                    PreparedStatement updateStmt = con.mkDataBase().prepareStatement(
                        "UPDATE bloodbank SET quantity = ?, bloodgroup = ?, last_donation_date = CURRENT_DATE WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching, Gap 7: Update last donation date
                ) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, bloodGroup);
                    updateStmt.setString(3, name);
                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        con.logAudit("UPDATE", name); // Gap 6: Log the UPDATE operation
                        statusLabel.setText("Success: Updated donor " + name + "!"); // Gap 3: Detailed feedback
                        JOptionPane.showMessageDialog(mainFrame, "Done Updating " + name); // Gap 3: Additional feedback
                        LOGGER.info("Successfully updated donor: " + name);
                        mainFrame.dispose();
                    } else {
                        statusLabel.setText("Error: No changes made to donor: " + name); // Gap 3: Detailed feedback
                        LOGGER.warning("No rows affected when updating donor: " + name);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Database Error: " + ex.getMessage()); // Gap 3: Detailed feedback
                LOGGER.log(Level.SEVERE, "Database error during update for " + name, ex);
            }
        });

        JPanel jp = new JPanel();
        jp.setLayout(experimentLayout);
        jp.add(nameLabel);
        jp.add(tfName);
        jp.add(bloodGroupLabel);
        jp.add(tfBloodGroup);
        jp.add(quantityLabel);
        jp.add(tfQuantity);
        jp.add(new JLabel()); // Spacer
        jp.add(okButton);
        controlPanel.add(jp); // This line should now work since controlPanel is initialized

        mainFrame.setVisible(true);
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