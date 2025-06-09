import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class DonorDetails extends JFrame {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel controlPanel;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DonorDetails app = new DonorDetails();
                app.showButtonDemo();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public DonorDetails() {
        prepareGUI();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Donor Details");
        mainFrame.setSize(800, 500);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(Color.RED);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        headerLabel = new JLabel("Blood Bank Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 25));
        headerLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("Search for donor details below.", JLabel.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.DARK_GRAY);

        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);

        mainFrame.add(headerLabel, BorderLayout.NORTH);
        mainFrame.add(controlPanel, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.setLocationRelativeTo(null);
    }

    public void showButtonDemo() throws SQLException {
        JPanel formPanel = new JPanel(new FlowLayout());
        JLabel nameLabel = new JLabel("Donor Name:");
        JTextField nameField = new JTextField(20);
        JButton searchButton = new JButton("Check Quantity");
        JButton stockButton = new JButton("View Stock"); // Gap 5: Button to view blood stock

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(searchButton);
        formPanel.add(stockButton);

        String[] columnNames = {"Donor ID", "Donor Name", "Blood Group", "Quantity", "Eligibility"}; // Gap 7: Add eligibility column
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        controlPanel.add(formPanel, BorderLayout.NORTH);
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        fillTable(tableModel);

        searchButton.addActionListener(e -> {
            String donorName = normalizeName(nameField.getText().trim()); // Gap 4: Normalize name for consistent case
            if (donorName.isEmpty()) {
                statusLabel.setText("Error: Please enter a donor name."); // Gap 3: Detailed feedback
                return;
            }

            try (
                DBConnection con = new DBConnection();
                PreparedStatement pst = con.mkDataBase().prepareStatement(
                    "SELECT quantity FROM bloodbank WHERE LOWER(name) = LOWER(?)") // Gap 4: Case-insensitive matching
            ) {
                pst.setString(1, donorName);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        statusLabel.setText("Success: Found donor " + donorName + "."); // Gap 3: Detailed feedback
                        JOptionPane.showMessageDialog(mainFrame,
                            "Quantity: " + rs.getInt("quantity"),
                            "Donor Found", JOptionPane.INFORMATION_MESSAGE); // Gap 3: Additional feedback
                    } else {
                        statusLabel.setText("Error: Donor not found: " + donorName); // Gap 3: Detailed feedback
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Database Error: " + ex.getMessage()); // Gap 3: Detailed feedback
            }
        });// Gap 3: Detailed feedback// Gap 3: Detailed feedback

        // Gap 5: Display blood stock by blood group
        stockButton.addActionListener(e -> {
            try (DBConnection con = new DBConnection()) {
                StringBuilder stockInfo = new StringBuilder("Blood Stock Totals (mL):\n");
                String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
                for (String group : bloodGroups) {
                    int stock = con.getBloodStock(group);
                    stockInfo.append(group).append(": ").append(stock).append("\n");
                }
                JOptionPane.showMessageDialog(mainFrame, stockInfo.toString(), "Stock Report", JOptionPane.INFORMATION_MESSAGE); // Gap 3: Additional feedback
                statusLabel.setText("Success: Stock report generated."); 
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Database Error: " + ex.getMessage()); 
            }
        });

        mainFrame.setVisible(true);
    }

    private void fillTable(DefaultTableModel model) throws SQLException {
        try (
            DBConnection con = new DBConnection();
            PreparedStatement pst = con.mkDataBase().prepareStatement("SELECT * FROM bloodbank");
            ResultSet rs = pst.executeQuery()
        ) {
            while (rs.next()) {
                String name = normalizeName(rs.getString("name")); // Gap 4: Normalize name for consistent case
                String bloodGroup = rs.getString("bloodgroup");
                int quantity = rs.getInt("quantity");
                java.sql.Date lastDonation = rs.getDate("last_donation_date");
                boolean eligible = con.isDonorEligible(name); // Gap 7: Check eligibility
                Object[] row = {
                    rs.getInt("id"),
                    name,
                    bloodGroup,
                    quantity,
                    eligible ? "Yes" : "No (wait " + (56 - (lastDonation != null ? daysSince(lastDonation) : 0)) + " days)" // Gap 7: Display eligibility status
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    // Gap 7: Calculate days since last donation for eligibility display
    private int daysSince(java.sql.Date date) {
        long diff = new java.util.Date().getTime() - date.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
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