import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TCPPacket {
    private int seq;
    private int ack;
//    private boolean synAck;
    private int destinationPort;
    private int dataLength;
    private String data;

    TCPPacket(int seq, int ack, int destinationPort, String pathToFile) throws IOException {
        this.seq = seq;
        this.ack = ack;
        this.destinationPort = destinationPort;
        this.data = this.readDataFromFile(pathToFile);
        this.dataLength = data.length();
//        this.synAck = synAck;
    }

    private String readDataFromFile(String pathToFile) throws IOException {
        String data;
        data = new String(Files.readAllBytes(Paths.get(pathToFile)));
        return data;
    }

    TCPPacket(DatagramPacket dp) {
         this.destinationPort = dp.getPort();
         byte[] buff = dp.getData();
         this.seq = byteToInt(0, buff);
         this.ack = byteToInt(4, buff);
         this.dataLength = byteToInt(8, buff);
         this.data = new String(buff, 12, dataLength);

    }

    private int byteToInt(int index, byte[] buff){
        return (int) ((buff[index] >> 24 & 0xff ) | (buff[index + 1] >> 16 & 0xff)
                | (buff[index + 2] >> 8 & 0xff) | ( buff[index + 3] & 0xff ));
    }

    private byte[] createPacket(){
        byte[] packet = new byte[1408];
        intToBytes(seq, 0, packet);
        intToBytes(ack, 4, packet);
        intToBytes(dataLength , 8, packet);
        injectBytes(packet, 12 , data.getBytes());
        return packet;
    }

    private void intToBytes(final int data, int index, byte[]buff) {
        buff[index] = (byte)((data >> 24) & 0xff);
        buff[index + 1] = (byte)((data >> 16) & 0xff);
        buff[index + 2] = (byte)((data >> 8) & 0xff);
        buff[index + 3] =(byte)((data) & 0xff);
    }

    private void injectBytes(byte[] toBuff, int index, byte[] buff){
        System.arraycopy(buff, 0, toBuff, index, buff.length );
    }

    public DatagramPacket getUDPPacket() throws Exception{
        byte[] buff = this.createPacket();
        DatagramPacket dp = new DatagramPacket(buff, buff.length );
        dp.setPort(destinationPort);
        dp.setAddress(InetAddress.getByName("127.0.0.1"));
        return dp;
    }

    public String getData() {
        return data;
    }
}
