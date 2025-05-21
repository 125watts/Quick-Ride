import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginUI extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;

    public LoginUI() {
        setTitle("QuickRide App - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Mot de passe:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginButton = new JButton("Se connecter");
        loginButton.addActionListener(e -> attemptLogin());
        panel.add(loginButton, gbc);

        add(panel);
        setVisible(true);
    }

    private void attemptLogin() {
        String name = nameField.getText();
        String password = new String(passwordField.getPassword());

        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/quickride_db",
                    "rider",
                    "ridepass");
            
            conn.setAutoCommit(false);
            
            String query = "SELECT id FROM clients WHERE nom = ? AND pwd = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, password);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // Login successful
                    int userId = rs.getInt("id");
                    conn.commit();
                    this.dispose();
                    new ClientUI(userId);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "Nom ou mot de passe incorrect",
                            "Erreur de connexion",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Driver MySQL non trouvé",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion à la base de données: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                }
            }
        }
    }
} 
