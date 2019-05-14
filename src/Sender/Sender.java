import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class Sender {
    public static void main(String[] args) throws Exception {
        TCPSocket tcpSocket = new TCPSocketImpl("127.0.0.1", 8001);
        tcpSocket.send("./src/Sender/miniFile.txt" , "127.0.0.1" , 9001);
        tcpSocket.close();
    }
}
