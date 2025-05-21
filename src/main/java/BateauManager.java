public class BateauManager extends VehiculeManager {
    private static BateauManager instance;

    private BateauManager() {}

    public static BateauManager getInstance() {
        if (instance == null) {
            instance = new BateauManager();
        }
        return instance;
    }
}
