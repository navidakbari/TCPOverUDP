import config.Config;
import tools.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
    private EnhancedDatagramSocket udp;
    private String data;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private enum handShakeStates {CLOSED , SYN_SENDING ,SYN_SENT , SENDING_ACK , ESTAB};
    private enum socketStates {IDEAL , HAND_SHAKE, GO_BACK_N_SEND, GO_BACK_N_RECEIVE};
    private handShakeStates handShakeState;
    private socketStates socketState;

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
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.handShakeState = handShakeStates.ESTAB;
        this.socketState = socketStates.GO_BACK_N_RECEIVE;
    }

    @Override
    public void send(String pathToFile, String destinationIp , int destinationPort) throws Exception {
        this.data = readDataFromFile(pathToFile);
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
                    break;
                case GO_BACK_N_RECEIVE:
                    break;
            }
        }
    }

    private String readDataFromFile(String pathToFile) throws IOException {
        String data;
        data = new String(Files.readAllBytes(Paths.get(pathToFile)));
        return data;
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
                        "");
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
                    TCPPacket sendPacket = new TCPPacket(destinationIp, destinationPort, sequenceNumber, acknowledgmentNumber + 1, true, false, "");
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
        throw new RuntimeException("Not implemented!");
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
