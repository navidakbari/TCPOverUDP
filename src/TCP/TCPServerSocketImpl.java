import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Base64;

public class TCPServerSocketImpl extends TCPServerSocket {
    private EnhancedDatagramSocket udp;

    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.udp = new EnhancedDatagramSocket(port);
    }


    @Override
    public void accept() throws Exception {

        byte[] buff = new byte[1408];
        DatagramPacket data = new DatagramPacket(buff, buff.length);
        this.udp.receive(data);
        TCPPacket packet = new TCPPacket(data);

        System.out.println(packet.getData());

        throw new RuntimeException("Not implemented!");

    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
