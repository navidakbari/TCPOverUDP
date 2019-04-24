import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;
    private String data;
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.udp = new EnhancedDatagramSocket(port);
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
        Random rand = new Random();
        TCPPacket packet = new TCPPacket(destinationIp , destinationPort , rand.nextInt( Integer.MAX_VALUE ) + 1 , 0 , false , true , "");
        this.udp.send(packet.getUDPPacket());
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
