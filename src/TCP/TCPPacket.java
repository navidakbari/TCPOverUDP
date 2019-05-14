import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TCPPacket {
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private int receiveWindow;
    private boolean SYN;
    private boolean ACK;
    private boolean FIN;
    private int destinationPort;
    private String destinationIp;
    private int dataLength;
    private byte[] data;

    TCPPacket(String destinationIp , int destinationPort , int sequenceNumber, int acknowledgmentNumber,boolean ACK , boolean SYN , int dataLength, byte[] data ) throws IOException {
        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.destinationPort = destinationPort;
        this.destinationIp = destinationIp;
        this.ACK = ACK;
        this.SYN = SYN;
        this.data = data;
        this.dataLength = dataLength;
        this.FIN = false;
    }

    public TCPPacket(String destinationIp , int destinationPort , int sequenceNumber, int acknowledgmentNumber,boolean ACK , boolean SYN , byte[] data, int receiveWindow) {
        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.receiveWindow = receiveWindow;
        this.SYN = SYN;
        this.ACK = ACK;
        this.destinationPort = destinationPort;
        this.destinationIp = destinationIp;
        this.data = data;
    }

    TCPPacket(DatagramPacket dp) {
         this.destinationPort = dp.getPort();
         byte[] buff = dp.getData();
         this.sequenceNumber = byteToInt(0, buff);
         this.acknowledgmentNumber = byteToInt(4, buff);
         this.dataLength = byteToInt(8, buff);
         this.ACK = byteToBool(12 , buff);
         this.SYN = byteToBool(13 , buff);
         this.FIN = byteToBool(14, buff);
         this.receiveWindow = byteToInt(15 , buff);
         this.data = Arrays.copyOfRange(buff, 19, this.dataLength + 19);
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
        byte[] packet = new byte[EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
        intToBytes(this.sequenceNumber, 0, packet);
        intToBytes(this.acknowledgmentNumber, 4, packet);
        intToBytes(this.dataLength , 8, packet);
        boolToByte(this.ACK , 12 , packet);
        boolToByte(this.SYN , 13 , packet);
        boolToByte(this.FIN, 14, packet);
        intToBytes(this.receiveWindow , 15 , packet);
        injectBytes(packet, 19 , data);
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

    public DatagramPacket getUDPPacket() throws UnknownHostException {
        byte[] buff = this.createPacket();
        DatagramPacket dp = new DatagramPacket(buff, buff.length );
        dp.setPort(this.destinationPort);
        dp.setAddress(InetAddress.getByName(this.destinationIp));
        return dp;
    }

    public byte[] getData() {
        byte[] exactData = new byte[dataLength];
        System.arraycopy(data, 0, exactData, 0, data.length );
        return exactData;
    }

    public Boolean getSynFlag(){
        return SYN;
    }

    public Boolean getAckFlag(){
        return ACK;
    }

    public void setFIN(boolean FIN) {
        this.FIN = FIN;
    }

    public boolean isFIN() {
        return FIN;
    }

    public int getSquenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int getReceiveWindow() { return receiveWindow; }
}
