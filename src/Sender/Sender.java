public class Sender {
    public static void main(String[] args) throws Exception {
        TCPSocket tcpSocket = new TCPSocketImpl("127.0.0.1", 9000);
        tcpSocket.send("./src/Sender/file.txt" , "127.0.0.1" , 8080);

//        tcpSocket.close();
//        tcpSocket.saveCongestionWindowPlot();
    }
}
