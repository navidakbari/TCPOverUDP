import config.Config;
import tools.ChunkMaker;
import tools.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Random;

class Window
{
    enum congestionStates {CONGESTION_AVOIDANCE , SLOW_START , FAST_RECOVERY};
    congestionStates congestionState;
    int sshtresh;
    int dupAckCount;
    int cwnd;
    int nextSeqNum;
    int base;
    HashMap<Integer, TCPPacket> packets = new HashMap<>();
}

public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private enum handShakeStates {CLOSED , SYN_SENDING ,SYN_SENT , SENDING_ACK , ESTAB};
    private enum socketStates {IDEAL , HAND_SHAKE, GO_BACK_N_SEND, GO_BACK_N_RECEIVE, CLOSE};
    private enum receiverStates {RECEIVE, WRITE_TO_FILE, SEND_ACK, SEND_DUP_ACK, CLOSE};
    private enum senderStates {SEND, RECEIVE_ACK, FINISH_SEND}
    private int expectedSequenceNumber;
    private handShakeStates handShakeState;
    private socketStates socketState;
    private senderStates senderState;
    private int lastAcked = Integer.MAX_VALUE;
    private int cwndCounter = 0;
    private Window win = new Window();
    private SocketTimer timer;

    public static final int chunkSize = EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES - 20;

    private receiverStates receiverState;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.udp = new EnhancedDatagramSocket(port);
        this.sequenceNumber = (new Random().nextInt( Integer.MAX_VALUE ) + 1)%10000;
        this.handShakeState = handShakeStates.CLOSED;
        this.socketState = socketStates.IDEAL;
        this.senderState = senderStates.SEND;
        this.udp.setSoTimeout(Config.TIMEOUT);
    }

    public TCPSocketImpl(String ip, int port, int sequenceNumber, int acknowledgmentNumber, EnhancedDatagramSocket udp) throws SocketException {
        super(ip, port);
        this.udp = udp;
        this.sequenceNumber = sequenceNumber;
        this.expectedSequenceNumber = acknowledgmentNumber + 1;
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.handShakeState = handShakeStates.ESTAB;
        this.socketState = socketStates.IDEAL;
    }

    @Override
    public void send(String pathToFile, String destinationIp , int destinationPort) throws Exception {
        boolean sending = true;
        while (sending)
        {
            switch (this.socketState){
                case IDEAL:
                    this.socketState = socketStates.HAND_SHAKE;
                    break;
                case HAND_SHAKE:
                    this.handShake(destinationIp, destinationPort);
                    break;
                case GO_BACK_N_SEND:
                    Log.SenderGoingToSendData();
                    this.goBackNSend(pathToFile, destinationIp, destinationPort);
                    break;
                case CLOSE:
                    sending = false;
                    saveCongestionWindowPlot();
                    break;
            }
        }
    }

    private void goBackNSend(String pathToFile, String destinationIp, int destinationPort) throws IOException
    {
        win.base = sequenceNumber ;
        win.nextSeqNum = sequenceNumber;
        win.cwnd = 1;
        win.sshtresh = 24;
        win.dupAckCount = 0;
        ChunkMaker chunkMaker = new ChunkMaker(pathToFile, chunkSize, win.base);
        this.udp.setSoTimeout(Integer.MAX_VALUE);
        timer = new SocketTimer(win, this.udp);
        timer.start();
        win.congestionState = Window.congestionStates.SLOW_START;

        boolean sending = true;
        while (sending)
        {
            switch (senderState)
            {
                case SEND:
                    sendPackets(chunkMaker, destinationIp, destinationPort);
                    break;
                case RECEIVE_ACK:
                    receiveACKs();
                    break;
                case FINISH_SEND:
                    sending = false;
                    System.out.println("FINIIIIIIIIISH");
                    timer.finish();
                    socketState = socketStates.CLOSE;
                    break;
            }
        }
    }

    private void receiveACKs() throws IOException
    {
        byte[] buff = new byte[1408];
        DatagramPacket data = new DatagramPacket(buff, buff.length);
        this.udp.receive(data);
        TCPPacket receivedPacket = new TCPPacket(data);

        System.out.println(win.congestionState);
        switch (win.congestionState) {
            case SLOW_START:
                slowStart(receivedPacket);
                break;
            case FAST_RECOVERY:
                fastRecovery(receivedPacket);
                break;
            case CONGESTION_AVOIDANCE:
                congestionAvoidance(receivedPacket);
                break;
        }
    }

    private void fastRecovery(TCPPacket receivedPacket) throws IOException {
        if(!receivedPacket.getSynFlag() && !receivedPacket.getAckFlag()) {
            if(lastAcked == receivedPacket.getAcknowledgmentNumber()){
                win.cwnd += 1;
                onWindowChange();
            }
            else if(receivedPacket.getAcknowledgmentNumber() >= win.base){
                win.cwnd = win.sshtresh != 0 ? win.sshtresh : 1;
                onWindowChange();
                win.dupAckCount = 0;
                moveBase(receivedPacket);
                cwndCounter = 0;
                win.congestionState = Window.congestionStates.CONGESTION_AVOIDANCE;
            }
        }

    }
    private void handleDupAck() {
        win.dupAckCount++;
        if(win.dupAckCount == 3) {
            win.sshtresh = win.cwnd/2;
            win.cwnd = win.sshtresh + 3;
            onWindowChange();
            win.congestionState = Window.congestionStates.FAST_RECOVERY;
            try {
                this.udp.send(win.packets.get(win.base).getUDPPacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveBase(TCPPacket receivedPacket){
        win.base = receivedPacket.getAcknowledgmentNumber() + 1;
        timer.restart();
        this.senderState = senderStates.SEND;
        lastAcked = receivedPacket.getAcknowledgmentNumber();
    }

    private void congestionAvoidance(TCPPacket receivedPacket) throws IOException {
        if(!receivedPacket.getSynFlag() && !receivedPacket.getAckFlag()) {
            if(lastAcked == receivedPacket.getAcknowledgmentNumber()){
               handleDupAck();
            }
            else if(receivedPacket.getAcknowledgmentNumber() >= win.base){
                if((++cwndCounter) == win.cwnd)
                {
                    win.cwnd ++;
                    onWindowChange();
                    cwndCounter = 0;
                }
                win.dupAckCount = 0;
                moveBase(receivedPacket);
            }
        }

    }
    private void slowStart(TCPPacket receivedPacket) throws IOException {
        if(!receivedPacket.getSynFlag() && !receivedPacket.getAckFlag()) {
            if(lastAcked == receivedPacket.getAcknowledgmentNumber()){
                handleDupAck();
            }
            else if(receivedPacket.getAcknowledgmentNumber() >= win.base){
                win.cwnd++;
                onWindowChange();
                win.dupAckCount = 0;
                moveBase(receivedPacket);
                if(win.cwnd > win.sshtresh) {
                    win.congestionState = Window.congestionStates.CONGESTION_AVOIDANCE;
                    cwndCounter = 0;
                }
            }
        }
    }

    private void sendPackets(ChunkMaker chunkMaker, String destinationIp, int destinationPort) throws IOException
    {
        while (true)
        {
            if(!isWindowFull() &&
                    chunkMaker.hasRemainingChunk(win.nextSeqNum))
            {
                win.packets.put(win.nextSeqNum,  new TCPPacket(
                        destinationIp,
                        destinationPort,
                        win.nextSeqNum ,
                        0,
                        false,
                        false,
                        chunkMaker.getChunk(win.nextSeqNum)));
                udp.send(win.packets.get(win.nextSeqNum).getUDPPacket());
                System.out.println("data with sent with seq : " + win.nextSeqNum);
                if(win.base == win.nextSeqNum)
                    timer.restart();
                win.nextSeqNum ++;
            }
            else{
                if(!chunkMaker.hasRemainingChunk(win.nextSeqNum)){
                    System.out.println("finish");
                    for (int i = 0; i < 100; i++) {
                        this.sendFin(destinationIp, destinationPort);
                    }
                    this.senderState = senderStates.FINISH_SEND;
                }
                else
                    this.senderState = senderStates.RECEIVE_ACK;
                break;
            }
        }
    }
    private void sendFin(String destinationIp, int destinationPort) throws IOException {
        TCPPacket sendPacket = new TCPPacket(
                destinationIp,
                destinationPort,
                sequenceNumber,
                0,
                false,
                false,
                new byte[0]);
        sendPacket.setFIN(true);
        this.udp.send(sendPacket.getUDPPacket());
    }


    private boolean isWindowFull()
    {
        return !(win.nextSeqNum < win.base + win.cwnd);
    }

    private void changeStateToSynSending(){
        this.handShakeState = handShakeStates.SYN_SENDING;
        Log.changeStateToSynSending();
    }

    private void sendingSyn(String destinationIp, int destinationPort) {
        while(true) {
            try {
                TCPPacket sendPacket = new TCPPacket(
                        destinationIp,
                        destinationPort,
                        sequenceNumber,
                        0,
                        false,
                        true,
                        new byte[0]);
                this.udp.send(sendPacket.getUDPPacket());
                this.handShakeState = handShakeStates.SYN_SENT;
                Log.sendingSynToReceiver();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void synSent() {
        while(true) {
            try {
                byte[] buff = new byte[1408];
                DatagramPacket data = new DatagramPacket(buff, buff.length);
                this.udp.receive(data);
                TCPPacket receivedPacket = new TCPPacket(data);
                if (receivedPacket.getAckFlag() && receivedPacket.getSynFlag() && receivedPacket.getAcknowledgmentNumber() == this.sequenceNumber + 1) {
                    this.acknowledgmentNumber = receivedPacket.getSquenceNumber();
                    this.sequenceNumber++;
                    this.handShakeState = handShakeStates.SENDING_ACK;
                    Log.senderReceivedSynAckPacket();
                    break;
                }
            }catch (SocketTimeoutException e){
                this.handShakeState = handShakeStates.SYN_SENDING;
                Log.senderHandshakeAckTimeout();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendingAck(String destinationIp, int destinationPort) {
        while (true) {
            try {
                for (int i = 0 ; i < Config.ACK_SENDIG_LIMIT_NUMBER ; i++) {
                    TCPPacket sendPacket = new TCPPacket(destinationIp, destinationPort, sequenceNumber, acknowledgmentNumber + 1, true, false, new byte[0]);
                    this.udp.send(sendPacket.getUDPPacket());
                }
                Log.senderSendAckToReceiver();
                this.handShakeState = handShakeStates.ESTAB;
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void establishing() {
        this.handShakeState = handShakeStates.ESTAB;
        this.socketState = socketStates.GO_BACK_N_SEND;
        Log.senderHandshakeFinished();
    }

    private void handShake(String destinationIp , int destinationPort) throws Exception {
        boolean handshaking = true;
        while(handshaking){
            switch (this.handShakeState){
                case CLOSED:
                    changeStateToSynSending();
                    break;
                case SYN_SENDING:
                    sendingSyn(destinationIp , destinationPort);
                    break;
                case SYN_SENT:
                    synSent();
                    break;
                case SENDING_ACK:
                    sendingAck(destinationIp , destinationPort);
                    break;
                case ESTAB:
                    establishing();
                    handshaking = false;
                    break;
            }
        }
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        this.socketState = socketStates.GO_BACK_N_RECEIVE;
        TCPPacket receivedPacket = null;
        this.receiverState = receiverStates.RECEIVE;
        this.udp.setSoTimeout(Integer.MAX_VALUE);
        boolean sending = true;
        while(sending){
            switch (receiverState){
                case RECEIVE:
                    receivedPacket = goBackNReceive();
                    break;
                case WRITE_TO_FILE:
                    printToFile(receivedPacket , pathToFile);
                    break;
                case SEND_DUP_ACK:
                    sendDupAck();
                    break;
                case SEND_ACK:
                    receiverSendAck();
                    break;
                case CLOSE:
                    this.socketState = socketStates.CLOSE;
                    sending = false;
                    break;
            }
        }
    }

    private void sendDupAck() {
        try {
            TCPPacket sendPacket = new TCPPacket(
                    this.ip,
                    this.port,
                    0,
                    this.acknowledgmentNumber,
                    false,
                    false,
                    new byte[0]);
            this.udp.send(sendPacket.getUDPPacket());
            this.receiverState = receiverStates.RECEIVE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiverSendAck() {
        try {
            TCPPacket sendPacket = new TCPPacket(
                    this.ip,
                    this.port,
                    0,
                    this.acknowledgmentNumber,
                    false,
                    false,
                    new byte[0]);
            this.udp.send(sendPacket.getUDPPacket());
            this.expectedSequenceNumber ++;
            this.receiverState = receiverStates.RECEIVE;
            System.out.println("send seq Ack : " + this.acknowledgmentNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printToFile(TCPPacket receivedPacket , String pathToFile) {
        try {
            OutputStream os = new FileOutputStream(new File(pathToFile), true);
            os.write(receivedPacket.getData());
            os.close();
            this.receiverState = receiverStates.SEND_ACK;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private TCPPacket goBackNReceive() {
        try {
            System.out.println("receive expected : " + this.expectedSequenceNumber);
            byte[] buff = new byte[EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
            DatagramPacket data = new DatagramPacket(buff, buff.length);
            this.udp.receive(data);
            TCPPacket receivedPacket = new TCPPacket(data);
            if(receivedPacket.isFIN())
            {
                receiverState = receiverStates.CLOSE;
                return null;
            }
            if(receivedPacket.getAckFlag() || receivedPacket.getSynFlag())
                return null;
            if(receivedPacket.getSquenceNumber() == this.expectedSequenceNumber){
                this.acknowledgmentNumber = receivedPacket.getSquenceNumber();
                this.receiverState = receiverStates.WRITE_TO_FILE;
                System.out.println("recevied packet with seq : " + this.acknowledgmentNumber);
                return receivedPacket;
            }else {
                this.receiverState = receiverStates.SEND_DUP_ACK;
                System.out.println("dup ack seq  : " + this.acknowledgmentNumber
                                    + " for " + receivedPacket.getSquenceNumber());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        this.udp.close();
    }

    @Override
    public long getSSThreshold() {
        return win.sshtresh;
    }

    @Override
    public long getWindowSize() {
        return win.cwnd;
    }
}