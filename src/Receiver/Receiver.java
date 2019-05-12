public class Receiver {
    public static void main(String[] args) throws Exception {
        TCPServerSocket tcpServerSocket = new TCPServerSocketImpl(9000);
        TCPSocket tcpSocket = tcpServerSocket.accept();
        tcpSocket.receive("./src/Receiver/aks.png" );
        tcpSocket.close();
        tcpServerSocket.close();
    }
}
