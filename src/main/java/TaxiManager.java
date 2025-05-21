public class TaxiManager extends VehiculeManager {
    private static TaxiManager instance;

    private TaxiManager() {}

    public static TaxiManager getInstance() {
        if (instance == null) {
            instance = new TaxiManager();
        }
        return instance;
    }
}
