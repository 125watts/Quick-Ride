import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class VehiculeManager {
    protected Queue<ClientRequest> clientQueue = new LinkedList<>();
    protected Set<Vehicule> availableVehicules = new HashSet<>();
    protected Set<Vehicule> notAvailableVehicules = new HashSet<>();

    // Connexion à la BDD
    private final String DB_URL = "jdbc:mysql://localhost:3306/quickride_db";
    private final String DB_USER = "rider";
    private final String DB_PASSWORD = "ridepass";

    public VehiculeManager() {
        loadVehiculesFromDatabase();
    }

    public void loadVehiculesFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, type, localisation FROM vehicules WHERE statut = 'disponible';";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String location = rs.getString("localisation");

                Vehicule v;
                if (type.equalsIgnoreCase("Taxi")) {
                    v = new Taxi(id, location);
                } else if (type.equalsIgnoreCase("Bateau")) {
                    v = new Bateau(id, location);
                } else {
                    continue; // type inconnu
                }

                availableVehicules.add(v);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des véhicules : " + e.getMessage());
        }
    }

    public void saveAttributionToDatabase(ClientRequest request, Vehicule vehicule) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Récupérer l'ID de la requête
            String getRequestIdSql = "SELECT id FROM requetes WHERE client_id = ? AND statut = 'en_attente' ORDER BY heure_demande DESC LIMIT 1";
            PreparedStatement getRequestIdStmt = conn.prepareStatement(getRequestIdSql);
            getRequestIdStmt.setInt(1, request.getClientId());
            ResultSet rs = getRequestIdStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Aucune requête en attente trouvée pour ce client");
            }

            int requestId = rs.getInt("id");

            // Mise à jour du statut du véhicule
            String updateVehiculeSql = "UPDATE vehicules SET statut = 'occupe' WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateVehiculeSql);
            updateStmt.setInt(1, vehicule.getId());
            updateStmt.executeUpdate();

            // Insertion de l'attribution
            String insertAttributionSql = "INSERT INTO attributions (id_requete, id_vehicule, heure_attrib) VALUES (?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement insertStmt = conn.prepareStatement(insertAttributionSql);
            insertStmt.setInt(1, requestId);  // Utilisation de l'ID de la requête au lieu du client_id
            insertStmt.setInt(2, vehicule.getId());
            insertStmt.executeUpdate();

            // Mise à jour du statut de la requête
            String updateRequestSql = "UPDATE requetes SET statut = 'attribuee' WHERE id = ?";
            PreparedStatement updateRequestStmt = conn.prepareStatement(updateRequestSql);
            updateRequestStmt.setInt(1, requestId);
            updateRequestStmt.executeUpdate();

            conn.commit();
            System.out.println("Attribution enregistrée avec succès dans la base de données.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            System.out.println("Erreur lors de l'enregistrement de l'attribution : " + e.getMessage());
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

    public boolean assignVehiculeToNextClient() {
        if (clientQueue.isEmpty()) {
            System.out.println("Aucune demande en attente.");
            return false;
        }

        ClientRequest nextClient = clientQueue.poll();
        Vehicule assignedVehicule = null;

        for (Vehicule vehicule : availableVehicules) {
            if (vehicule.getLocation().equalsIgnoreCase(nextClient.getLocation())) {
                assignedVehicule = vehicule;
                break;
            }
        }

        if (assignedVehicule == null) {
            System.out.println("Aucun véhicule disponible dans la ville demandée: " + nextClient.getLocation());
            return false;
        }

        availableVehicules.remove(assignedVehicule);
        notAvailableVehicules.add(assignedVehicule);
        saveAttributionToDatabase(nextClient, assignedVehicule);
        System.out.println(assignedVehicule.getClass().getSimpleName() + " ID " + assignedVehicule.getId() +
                " attribué au client " + nextClient.getClientId() + " à " + nextClient.getLocation());
        return true;
    }

    public List<Vehicule> getAllVehicules() {
        return new ArrayList<>(availableVehicules);
    }

    public boolean getVehiculesPresence() {
        return !(availableVehicules.isEmpty() && notAvailableVehicules.isEmpty());
    }

    public void addClientRequest(ClientRequest request) {
        clientQueue.add(request);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false); // Désactive l'auto-commit pour gérer la transaction

            String sql = "INSERT INTO requetes (client_id, localisation, heure_demande) VALUES (?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, request.getClientId());
            stmt.setString(2, request.getLocation());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                conn.commit(); // Valide la transaction
                System.out.println("Requête client insérée dans la base de données.");
            } else {
                conn.rollback(); // Annule la transaction en cas d'échec
                System.out.println("Échec de l'insertion de la requête.");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Ceci annule la transaction en cas d'erreur
                } catch (SQLException ex) {
                    System.out.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            System.out.println("Erreur SQL lors de l'insertion de la requête client : " + e.getMessage());
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

    public boolean cancelRide(int clientId) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Récupérer l'ID du véhicule attribué
            String getVehiculeSql = "SELECT a.id_vehicule, v.type, v.localisation FROM attributions a " +
                                  "JOIN requetes r ON a.id_requete = r.id " +
                                  "JOIN vehicules v ON a.id_vehicule = v.id " +
                                  "WHERE r.client_id = ? AND r.statut = 'attribuee'";
            PreparedStatement getVehiculeStmt = conn.prepareStatement(getVehiculeSql);
            getVehiculeStmt.setInt(1, clientId);
            ResultSet rs = getVehiculeStmt.executeQuery();

            if (rs.next()) {
                int vehiculeId = rs.getInt("id_vehicule");
                String type = rs.getString("type");
                String location = rs.getString("localisation");

                // Mettre à jour le statut du véhicule
                String updateVehiculeSql = "UPDATE vehicules SET statut = 'disponible' WHERE id = ?";
                PreparedStatement updateVehiculeStmt = conn.prepareStatement(updateVehiculeSql);
                updateVehiculeStmt.setInt(1, vehiculeId);
                updateVehiculeStmt.executeUpdate();

                // Mettre à jour le statut de la requête
                String updateRequestSql = "UPDATE requetes SET statut = 'annulee' WHERE client_id = ? AND statut = 'attribuee'";
                PreparedStatement updateRequestStmt = conn.prepareStatement(updateRequestSql);
                updateRequestStmt.setInt(1, clientId);
                updateRequestStmt.executeUpdate();

                // Supprimer l'attribution
                String deleteAttributionSql = "DELETE FROM attributions WHERE id_vehicule = ?";
                PreparedStatement deleteAttributionStmt = conn.prepareStatement(deleteAttributionSql);
                deleteAttributionStmt.setInt(1, vehiculeId);
                deleteAttributionStmt.executeUpdate();

                // Mettre à jour les collections en mémoire
                Vehicule vehiculeToMove = null;
                for (Vehicule v : notAvailableVehicules) {
                    if (v.getId() == vehiculeId) {
                        vehiculeToMove = v;
                        break;
                    }
                }

                if (vehiculeToMove != null) {
                    notAvailableVehicules.remove(vehiculeToMove);
                    availableVehicules.add(vehiculeToMove);
                }

                conn.commit();
                System.out.println("Course annulée avec succès.");
                return true;
            } else {
                conn.rollback();
                System.out.println("Aucune course active trouvée pour ce client.");
                return false;
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            System.out.println("Erreur lors de l'annulation de la course : " + e.getMessage());
            return false;
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

