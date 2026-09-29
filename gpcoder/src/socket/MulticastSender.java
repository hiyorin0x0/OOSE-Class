package socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.Collections;
 
public class MulticastSender {
 
    public static final String GROUP_ADDRESS = "224.0.0.1";
    public static final int PORT = 8888;
 
    public static void main(String[] args) throws InterruptedException {
        MulticastSocket socket = null;
        try {
            // Get the address that we are going to connect to.
            InetAddress address = InetAddress.getByName(GROUP_ADDRESS);
 
            // Create a new Multicast socket
            socket = new MulticastSocket();
            NetworkInterface network = selectNetworkInterface();
            socket.setNetworkInterface(network);
            System.out.println("Sending on " + network.getDisplayName());
 
            DatagramPacket outPacket = null;
            long counter = 0;
            while (true) {
                String msg = "Sent message No. " + counter;
                counter++;
                byte[] data = msg.getBytes(StandardCharsets.UTF_8);
                outPacket = new DatagramPacket(data, data.length, address, PORT);
                socket.send(outPacket);
                System.out.println("Server sent packet with msg: " + msg);
                Thread.sleep(1000); // Sleep 1 second before sending the next message
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    static NetworkInterface selectNetworkInterface() throws IOException {
        // Optional VM argument: -Dmulticast.interface=<interface name or local IPv4>
        String configured = System.getProperty("multicast.interface");
        if (configured != null && !configured.isBlank()) {
            NetworkInterface network = NetworkInterface.getByName(configured);
            if (network == null) {
                network = NetworkInterface.getByInetAddress(InetAddress.getByName(configured));
            }
            if (network != null && usable(network)) {
                return network;
            }
            throw new IOException("Interface is unavailable for IPv4 multicast: " + configured);
        }

        NetworkInterface fallback = null;
        for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (usable(network)) {
                if (!network.isVirtual() && network.getHardwareAddress() != null) {
                    return network;
                }
                fallback = network;
            }
        }
        if (fallback != null) {
            return fallback;
        }
        throw new IOException("No active IPv4 multicast interface. Connect Wi-Fi or Ethernet.");
    }

    private static boolean usable(NetworkInterface network) throws SocketException {
        return network.isUp() && !network.isLoopback() && network.supportsMulticast()
                && Collections.list(network.getInetAddresses()).stream()
                        .anyMatch(address -> address instanceof Inet4Address);
    }
}
