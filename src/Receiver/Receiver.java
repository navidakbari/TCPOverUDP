public class Receiver {
    public static void main(String[] args) throws Exception {
        TCPServerSocket tcpServerSocket = new TCPServerSocketImpl(9001);
        TCPSocket tcpSocket = tcpServerSocket.accept();
        tcpSocket.receive("./src/Receiver/miniFile.txt" );
        tcpSocket.close();
        tcpServerSocket.close();
    }
}
