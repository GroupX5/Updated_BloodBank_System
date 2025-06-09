import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Home {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Home app = new Home();
            app.showButtonDemo();
        });
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Blood Bank Management System");
        mainFrame.setSize(700, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setBackground(Color.RED);
        mainFrame.setLayout(new BorderLayout());

        headerLabel = new JLabel("Blood Bank Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 27));
        headerLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setPreferredSize(new Dimension(100, 30));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.DARK_GRAY);

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 5, 10, 10));

        mainFrame.add(headerLabel, BorderLayout.NORTH);
        mainFrame.add(controlPanel, BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);
    }

    public void showButtonDemo() {
        prepareGUI();

        JButton donorsButton = new JButton("Donors");
        JButton donationsButton = new JButton("Blood Donations");
        JButton addButton = new JButton("Add Donor");
        JButton updateButton = new JButton("Update Donor");
        JButton deleteButton = new JButton("Delete Donor");

        donorsButton.addActionListener(e -> {
            try {
                new DonorDetails().showButtonDemo();
            } catch (SQLException ex) {
                Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(mainFrame, "Error displaying donors: " + ex.getMessage());
            }
        });

        addButton.addActionListener(e -> new AddDonor().showButtonDemo());
        updateButton.addActionListener(e -> new UpdateDonor().showButtonDemo());
        deleteButton.addActionListener(e -> new DeleteDonor().showButtonDemo());
        donationsButton.addActionListener(e -> JOptionPane.showMessageDialog(mainFrame, "Blood Donations not implemented yet."));

        controlPanel.add(updateButton);
        controlPanel.add(addButton);
        controlPanel.add(donationsButton);
        controlPanel.add(donorsButton);
        controlPanel.add(deleteButton);

        mainFrame.setVisible(true);
    }
}
