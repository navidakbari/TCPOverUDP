import config.Config;
import tools.ChunkMaker;
import tools.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

class Window
{
    int nextSeqNum;
    int base;
    TCPPacket[] packets = new TCPPacket[TCPSocketImpl.windowSize];
}
public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private enum handShakeStates {CLOSED , SYN_SENDING ,SYN_SENT , SENDING_ACK , ESTAB};
    private enum socketStates {IDEAL , HAND_SHAKE, GO_BACK_N_SEND, GO_BACK_N_RECEIVE};
    private enum receiverStates {RECEIVE, WRITE_TO_FILE, SEND_ACK};
    private int expectedSequenceNumber;
    private handShakeStates handShakeState;
    private socketStates socketState;
    public static final int chunkSize = EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES;
    public static final int windowSize = 10;
    private receiverStates receiverState;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.udp = new EnhancedDatagramSocket(port);
        this.sequenceNumber = (new Random().nextInt( Integer.MAX_VALUE ) + 1)%10000;
        this.handShakeState = handShakeStates.CLOSED;
        this.socketState = socketStates.IDEAL;
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
        while (true)
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
            }
        }
    }
    private void goBackNSend(String pathToFile, String destinationIp, int destinationPort) throws IOException
    {
        ChunkMaker chunkMaker = new ChunkMaker(pathToFile, chunkSize);
        Window win = new Window();
        win.base = sequenceNumber;
        win.nextSeqNum = sequenceNumber;
        this.udp.setSoTimeout(Integer.MAX_VALUE);

        SocketTimer timer = new SocketTimer(win);
        while(true)
        {
            if(!isWindowFull(win.nextSeqNum, win.base) &&
                    !chunkMaker.hasRemainingChunk(sequenceNumber))
            {
                win.packets[ win.nextSeqNum - win.base] = new TCPPacket(
                        destinationIp,
                        destinationPort,
                        win.nextSeqNum,
                        0,
                        false,
                        false,
                        chunkMaker.getChunk(win.nextSeqNum));
                udp.send(win.packets[win.nextSeqNum].getUDPPacket());
                if(win.base == win.nextSeqNum)
                    timer.start();
                win.nextSeqNum ++;
            }
            else
            {
                byte[] buff = new byte[1408];
                DatagramPacket data = new DatagramPacket(buff, buff.length);
                this.udp.receive(data);
                TCPPacket receivedPacket = new TCPPacket(data);
                if(!receivedPacket.getSynFlag() && !receivedPacket.getAckFlag()) {
                    win.base = (receivedPacket.getAcknowledgmentNumber() / TCPSocketImpl.chunkSize) + 1;
                    if (win.base == win.nextSeqNum)
                        timer.stop();
                    else
                        timer.start();
                }
            }
        }
    }
    private boolean isWindowFull(int nextSeqNum, int base)
    {
        return !(nextSeqNum < base + TCPSocketImpl.windowSize);
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
        while(true){
            switch (receiverState){
                case RECEIVE:
                    receivedPacket = goBackNReceive();
                case WRITE_TO_FILE:
                    printToFile(receivedPacket , pathToFile);
                case SEND_ACK:
                    receiverSendAck();
            }
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printToFile(TCPPacket receivedPacket , String pathToFile) {
        //TODO: CHANGE TO BYTE FILE
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(pathToFile, "UTF-8");
            writer.print(receivedPacket.getData());
            writer.close();
            this.receiverState = receiverStates.SEND_ACK;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private TCPPacket goBackNReceive() {
        try {
            byte[] buff = new byte[EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
            DatagramPacket data = new DatagramPacket(buff, buff.length);
            this.udp.receive(data);
            TCPPacket receivedPacket = new TCPPacket(data);
            if(receivedPacket.getSquenceNumber() == this.expectedSequenceNumber){
                this.acknowledgmentNumber = receivedPacket.getSquenceNumber();
                this.receiverState = receiverStates.WRITE_TO_FILE;
                return receivedPacket;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        throw new RuntimeException("Not implemented!");
    }
}
