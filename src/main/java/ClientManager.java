public class ClientManager {

    private static volatile ClientManager instance;
    private static final Object lock = new Object();

    private ClientManager() {}

    public static ClientManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ClientManager();
                }
            }
        }
        return instance;
    }


}
