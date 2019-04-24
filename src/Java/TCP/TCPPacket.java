import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class TCPPacket {
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private boolean SYN;
    private boolean ACK;
    private int destinationPort;
    private String destinationIp;
    private int dataLength;
    private String data;

    TCPPacket(String destinationIp , int destinationPort , int sequenceNumber, int acknowledgmentNumber,boolean ACK , boolean SYN , String data) throws IOException {
        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.destinationPort = destinationPort;
        this.destinationIp = destinationIp;
        this.ACK = ACK;
        this.SYN = SYN;
        this.data = data;
        this.dataLength = data.length();
    }

    TCPPacket(DatagramPacket dp) {
         this.destinationPort = dp.getPort();
         byte[] buff = dp.getData();
         this.sequenceNumber = byteToInt(0, buff);
         this.acknowledgmentNumber = byteToInt(4, buff);
         this.dataLength = byteToInt(8, buff);
         this.ACK = byteToBool(12 , buff);
         this.SYN = byteToBool(13 , buff);
         this.data = new String(buff, 14, dataLength);
         System.out.println("destinationPort= " +destinationPort);
        System.out.println("sequenceNumber= " +sequenceNumber);
        System.out.println("acknowledgmentNumber= " +acknowledgmentNumber);
        System.out.println("ACK = " +ACK);
        System.out.println("SYN = " +SYN);

    }

    private boolean byteToBool(int index, byte[] buff) {
        if(buff[index] == (byte)1){
            return true;
        }else{
            return false;
        }
    }

    private int byteToInt(int index, byte[] buff){
        ByteBuffer wrapped = ByteBuffer.wrap(buff , index , 4); // big-endian by default
        int num = wrapped.getInt();
        return num;
    }

    private byte[] createPacket(){
        byte[] packet = new byte[1408];
        intToBytes(this.sequenceNumber, 0, packet);
        intToBytes(this.acknowledgmentNumber, 4, packet);
        intToBytes(this.dataLength , 8, packet);
        boolToByte(this.ACK , 12 , packet);
        boolToByte(this.SYN , 13 , packet);
        injectBytes(packet, 14 , data.getBytes());
        return packet;
    }

    private void boolToByte(final boolean data, int index, byte[] buff) {
        if(data == true){
            buff[index] = (byte)1;
        }else{
            buff[index] = (byte)0;
        }

    }

    private void intToBytes(final int data, int index, byte[]buff) {
        byte[] array = ByteBuffer.allocate(4).putInt(data).array();
        buff[index] = array[0];
        buff[index + 1] = array[1];
        buff[index + 2] = array[2];
        buff[index + 3] = array[3];
    }

    private void injectBytes(byte[] toBuff, int index, byte[] buff){
        System.arraycopy(buff, 0, toBuff, index, buff.length );
    }

    public DatagramPacket getUDPPacket() throws Exception{
        byte[] buff = this.createPacket();
        DatagramPacket dp = new DatagramPacket(buff, buff.length );
        dp.setPort(this.destinationPort);
        dp.setAddress(InetAddress.getByName(this.destinationIp));
        System.out.println("destinationPort= " +destinationPort);
        System.out.println("sequenceNumber= " +sequenceNumber);
        System.out.println("acknowledgmentNumber= " +acknowledgmentNumber);
        System.out.println("ACK = " +ACK);
        System.out.println("SYN = " +SYN);
        return dp;
    }

    public String getData() {
        return data;
    }

    public Boolean getSynFlag(){
        return SYN;
    }

    public Boolean getAckFlag(){
        return ACK;
    }

    public int getSquenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

}
