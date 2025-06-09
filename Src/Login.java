import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Login extends JFrame {
    private JLabel idLabel, passLabel, headerLabel, devInfo;
    private JTextField id;
    private JPasswordField pass;
    private JButton submit;

    private static final String ADMIN_USER = "Admin";
    private static final String ADMIN_PASS = "Admin123";

    public Login() {
        setTitle("Blood Bank Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        init();
        setVisible(true);
    }

    private void init() {
        setLayout(null); 

        headerLabel = new JLabel("Blood Bank Management System");
        headerLabel.setBounds(190, 1, 370, 150);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.red);
        add(headerLabel);

        idLabel = new JLabel("Username");
        idLabel.setBounds(190, 110, 150, 50);
        idLabel.setFont(new Font(null, Font.BOLD, 20));
        add(idLabel);

        passLabel = new JLabel("Password");
        passLabel.setBounds(190, 165, 100, 50);
        passLabel.setFont(new Font(null, Font.BOLD, 20));
        add(passLabel);

        id = new JTextField();
        id.setBounds(300, 125, 200, 30);
        add(id);

        pass = new JPasswordField();
        pass.setBounds(300, 175, 200, 30);
        add(pass);

        submit = new JButton("Login");
        submit.setBounds(400, 230, 100, 25);
        submit.addActionListener(this::submitActionPerformed);
        add(submit);
        
        devInfo = new JLabel("Enhanced by Group-X.");
        devInfo.setBounds(200, 300, 400, 30);
        devInfo.setFont(new Font("Arial", Font.PLAIN, 15));
        devInfo.setOpaque(true);
        devInfo.setBackground(Color.DARK_GRAY);
        devInfo.setForeground(Color.white);
        add(devInfo);
    }

    private void submitActionPerformed(ActionEvent evt) {
        String username = id.getText().trim();
        String password = new String(pass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!");
            return;
        }

        if (username.equals(ADMIN_USER) && password.equals(ADMIN_PASS)) {
            setVisible(false);
            new Home().showButtonDemo();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid username or password!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}
