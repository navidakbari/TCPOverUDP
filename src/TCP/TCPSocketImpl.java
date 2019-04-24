import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;
    private String data;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.udp = new EnhancedDatagramSocket(port);
        this.sequenceNumber = (new Random().nextInt( Integer.MAX_VALUE ) + 1)%10000;

    }

    @Override
    public void send(String pathToFile, String destinationIp , int destinationPort) throws Exception {
        this.data = readDataFromFile(pathToFile);
        if(this.handShake(destinationIp , destinationPort)){
//            TCPPacket packet = new TCPPacket(8080 , 200, 100, 8080, pathToFile);
//            this.udp.send(packet.getUDPPacket());
        }
    }

    private String readDataFromFile(String pathToFile) throws IOException {
        String data;
        data = new String(Files.readAllBytes(Paths.get(pathToFile)));
        return data;
    }

    private boolean handShake(String destinationIp , int destinationPort) throws Exception {
        TCPPacket sendPacket = new TCPPacket(destinationIp , destinationPort , sequenceNumber , 0 , false , true , "");
        this.udp.send(sendPacket.getUDPPacket());
        byte[] buff = new byte[1408];
        DatagramPacket data = new DatagramPacket(buff, buff.length);
        this.udp.receive(data);
        TCPPacket recievedPacket = new TCPPacket(data);
        this.acknowledgmentNumber = recievedPacket.getSquenceNumber();
        this.sequenceNumber++;
        sendPacket = new TCPPacket(destinationIp, destinationPort, sequenceNumber, acknowledgmentNumber+1, true, false, "");
        this.udp.send(sendPacket.getUDPPacket());
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
