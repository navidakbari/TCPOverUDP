import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class TCPServerSocketImpl extends TCPServerSocket {
    private EnhancedDatagramSocket udp;
    private int port;
    private int destinationPort;
    private InetAddress destinationIp;
    private int sequenceNumber;
    private int acknowledgmentNumber;

    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.port = port;
        this.udp = new EnhancedDatagramSocket(port);
    }


    @Override
    public void accept() throws Exception {
        if(handShake()){

        }

    }

    private boolean handShake() throws Exception {
        byte[] buff = new byte[1408];
        DatagramPacket data = new DatagramPacket(buff, buff.length);
        this.udp.receive(data);
        TCPPacket recievedPacket = new TCPPacket(data);
        this.destinationPort = data.getPort();
        this.destinationIp = data.getAddress();
        this.acknowledgmentNumber = recievedPacket.getSquenceNumber();
        this.sequenceNumber = (new Random().nextInt( Integer.MAX_VALUE ) + 1)%10000;
        TCPPacket sendPacket = new TCPPacket(this.destinationIp.getHostAddress() , this.destinationPort , sequenceNumber , acknowledgmentNumber+1 , true , true , "");
        this.udp.send(sendPacket.getUDPPacket());
        buff = new byte[1408];
        data = new DatagramPacket(buff, buff.length);
        this.udp.receive(data);
        recievedPacket = new TCPPacket(data);
        return true;
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
