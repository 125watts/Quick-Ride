import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientUI {
    private VehiculeManager taxiManager = TaxiManager.getInstance();
    private final int clientId;
    private JLabel statusLabel;
    private JButton cancelButton;

    public ClientUI(int clientId) {
        this.clientId = clientId;
        
        JFrame frame = new JFrame("QuickRide - Client");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new GridLayout(5, 1));

        JTextField locationField = new JTextField("Entrez votre ville");
        JButton requestButton = new JButton("Commander un taxi");
        statusLabel = new JLabel("Statut : en attente");
        cancelButton = new JButton("Annuler la course");
        cancelButton.setEnabled(false);

        panel.add(new JLabel("Votre localisation :"));
        panel.add(locationField);
        panel.add(requestButton);
        panel.add(statusLabel);
        panel.add(cancelButton);

        requestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = locationField.getText().trim();
                if (!location.isEmpty()) {
                    ClientRequest request = new ClientRequest(clientId, location);
                    taxiManager.addClientRequest(request);
                    boolean success = taxiManager.assignVehiculeToNextClient();
                    if (success) {
                        statusLabel.setText("Un taxi vous a été attribué.");
                        cancelButton.setEnabled(true);
                        requestButton.setEnabled(false);
                    } else {
                        statusLabel.setText("Aucun taxi disponible dans votre ville.");
                    }
                } else {
                    statusLabel.setText("Veuillez entrer une localisation valide.");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean success = taxiManager.cancelRide(clientId);
                if (success) {
                    statusLabel.setText("Course annulée avec succès.");
                    cancelButton.setEnabled(false);
                    requestButton.setEnabled(true);
                } else {
                    statusLabel.setText("Impossible d'annuler la course.");
                }
            }
        });

        frame.setVisible(true);
    }
}
