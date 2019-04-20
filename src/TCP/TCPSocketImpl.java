import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.udp = new EnhancedDatagramSocket(port);

    }

    @Override
    public void send(String pathToFile) throws Exception {


        TCPPacket packet = new TCPPacket(200, 100, 8080, "Navid");
        this.udp.send(packet.getUDPPacket());
//        throw new RuntimeException("Not implemented!");
    }

    private boolean handShake()
    {

        return true;
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        throw new RuntimeException("Not implemented!");
    }
}
