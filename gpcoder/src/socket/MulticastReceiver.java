package socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
 
public class MulticastReceiver {
 
    public static final byte[] BUFFER = new byte[4096];
 
    public static void main(String[] args) {
        MulticastSocket socket = null;
        DatagramPacket inPacket = null;
        try {
            // Get the address that we are going to connect to.
            InetAddress address = InetAddress.getByName(MulticastSender.GROUP_ADDRESS);
            
            // Create a new Multicast socket
            socket = new MulticastSocket(MulticastSender.PORT);
 
            // Joint the Multicast group
            NetworkInterface network = MulticastSender.selectNetworkInterface();
            socket.joinGroup(new InetSocketAddress(address, MulticastSender.PORT), network);
            System.out.println("Listening on " + network.getDisplayName() + ":" + MulticastSender.PORT);
 
            while (true) {
                // Receive the information and print it.
                inPacket = new DatagramPacket(BUFFER, BUFFER.length);
                socket.receive(inPacket);
                String msg = new String(BUFFER, 0, inPacket.getLength(), StandardCharsets.UTF_8);
                System.out.println("From " + inPacket.getAddress() + " Msg : " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
