public class Taxi implements Vehicule{
    private final int id;
    private String status;  // "Disponible", "Occupé", "Hors Service"
    private String location;

    public Taxi(int id, String location) {
        this.id = id;
        this.status = "Disponible";
        this.location = location;
    }

    public int getId() { return id; }
    public String getStatus() { return status; }
    public String getLocation() { return location; }

    public void setStatus(String status) { this.status = status; }
    public void setLocation(String location) { this.location = location; }
}
