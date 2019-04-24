import java.net.DatagramPacket;

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
        System.out.print(packet.getSynFlag());
        System.out.print(packet.getAckFlag());


//        throw new RuntimeException("Not implemented!");

    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
