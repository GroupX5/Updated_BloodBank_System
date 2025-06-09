import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class AddDonor {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JLabel nameLabel, bloodGroupLabel, quantityLabel;
    private GridLayout experimentLayout = new GridLayout(0, 2);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddDonor swingControlDemo = new AddDonor();
            swingControlDemo.showButtonDemo();
        });
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Insert a New Donor");
        mainFrame.setSize(700, 400);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(Color.RED);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        headerLabel = new JLabel("Blood Bank Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 27));
        headerLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("Enter details to add a new donor.", JLabel.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.DARK_GRAY);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBackground(Color.RED);

        mainFrame.add(headerLabel, BorderLayout.NORTH);
        mainFrame.add(controlPanel, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);
    }

    public void showButtonDemo() {
        prepareGUI();

        nameLabel = new JLabel("Enter Donor Name");
        JTextField tfName = new JTextField(20);

        bloodGroupLabel = new JLabel("Enter Blood Group");
        JTextField tfBloodGroup = new JTextField(20);

        quantityLabel = new JLabel("Enter Quantity (max 500 mL)"); // Gap 5: Indicate quantity limit in label
        JTextField tfQuantity = new JTextField(20);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String name = normalizeName(tfName.getText().trim()); // Gap 4: Normalize name for consistent case
            String bloodGroup = tfBloodGroup.getText().trim().toUpperCase();
            String quantityStr = tfQuantity.getText().trim();

            // Validate inputs
            if (name.isEmpty() || bloodGroup.isEmpty() || quantityStr.isEmpty()) {
                statusLabel.setText("Error: All fields are required!"); // Gap 3: Detailed feedback
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
                return;
            }// Gap 3: Detailed feedback// Gap 3: Detailed feedback

            // Gap 5: Validate quantity limits
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0 || quantity > 500) {
                    statusLabel.setText("Error: Quantity must be between 1 and 500 mL!"); 
                    return;
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Quantity must be a valid number!"); 
                return;
            }

            // Gap 2: Check for duplicate donor and Gap 7: Check eligibility
            try (DBConnection con = new DBConnection()) {
                if (con.donorExists(name)) {
                    int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Donor " + name + " already exists. Proceed anyway?",
                        "Duplicate Donor", JOptionPane.YES_NO_OPTION); // Gap 2: Prompt for duplicate
                    if (confirm != JOptionPane.YES_OPTION) {
                        statusLabel.setText("Operation cancelled: Donor already exists.");
                        return;
                    }
                }

                if (!con.isDonorEligible(name)) {
                    statusLabel.setText("Error: " + name + " is not eligible (less than 56 days since last donation)."); // Gap 7 & Gap 3: Eligibility check and Feedback
                    return;
                }

                // Insert donor
                try ( 
                    PreparedStatement pst = con.mkDataBase().prepareStatement(
                        "INSERT INTO bloodbank(name, bloodgroup, quantity, last_donation_date) VALUES (?, ?, ?, CURRENT_DATE)") // Gap 7: Track last donation date
                ) {
                    pst.setString(1, name);
                    pst.setString(2, bloodGroup);
                    pst.setInt(3, quantity);
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        con.logAudit("ADD", name); // Gap 6: Log the ADD operation
                        statusLabel.setText("Success: Donor " + name + " added!"); // Gap 3: Detailed feedback
                        JOptionPane.showMessageDialog(mainFrame, "Donor " + name + " added successfully!"); // Gap 3: Additional feedback
                        mainFrame.dispose();
                    } else {
                        statusLabel.setText("Error: Failed to add donor: " + name); // Gap 3: Detailed feedback
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Database Error: " + ex.getMessage()); // Gap 3: Detailed feedback
            }
        });

        JPanel formPanel = new JPanel();
        formPanel.setLayout(experimentLayout);
        formPanel.add(nameLabel);
        formPanel.add(tfName);
        formPanel.add(bloodGroupLabel);
        formPanel.add(tfBloodGroup);
        formPanel.add(quantityLabel);
        formPanel.add(tfQuantity);
        formPanel.add(new JLabel()); // Spacer
        formPanel.add(okButton);

        controlPanel.add(formPanel);
        mainFrame.setLocationRelativeTo(null);
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