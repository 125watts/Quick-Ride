public class ClientRequest {
    private final int clientId;
    private String location;

    public ClientRequest(int clientId, String location) {
        this.clientId = clientId;
        this.location = location;
    }

    public int getClientId() { return clientId; }
    public String getLocation() { return location; }
}
