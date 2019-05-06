import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class Sender {
    public static void main(String[] args) throws Exception {
        TCPSocket tcpSocket = new TCPSocketImpl("127.0.0.1", 9000);
        tcpSocket.send("./src/Sender/file.txt" , "172.20.10.4" , 8080);
//        try {
//            EnhancedDatagramSocket datagramSocket = new EnhancedDatagramSocket(8000);
//            datagramSocket.setSoTimeout(2000);
//            byte[] buff = new byte[1408];
//            datagramSocket.receive(new DatagramPacket(buff, buff.length));
//        }
//        catch (SocketTimeoutException e)
//        {
//            System.out.println("timeout ");
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            System.out.println("IO");
//            e.printStackTrace();
//        }

//        tcpSocket.close();
//        tcpSocket.saveCongestionWindowPlot();
    }
}
