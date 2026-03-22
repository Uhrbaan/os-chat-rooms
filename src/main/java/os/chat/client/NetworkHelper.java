package os.chat.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

// Helper function to get the localHost public IP.
public class NetworkHelper {
    public static String getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Current IP Address: " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
