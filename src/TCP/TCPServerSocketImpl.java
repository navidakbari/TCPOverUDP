import config.Config;
import tools.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

public class TCPServerSocketImpl extends TCPServerSocket {
    private EnhancedDatagramSocket udp;
    private int port;
    private int destinationPort;
    private InetAddress destinationIp;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private enum handshakeStates  {CLOSED,LISTEN, ACK_SENDING ,SYN_REC, ESTB};
    private handshakeStates handshakeState;

    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.port = port;
        this.udp = new EnhancedDatagramSocket(port);
        this.handshakeState = handshakeStates.CLOSED;
        this.sequenceNumber = (new Random().nextInt( Integer.MAX_VALUE ) + 1)%10000;
        this.udp.setSoTimeout(Config.TIMEOUT);
    }


    //TODO: This function should return a TCP SOCKET
    @Override
    public void accept() {
        boolean finishHandshake = false;
        while (!finishHandshake)
        {
            switch (handshakeState){
                case CLOSED:
                    changeStateToListen();
                    break;
                case LISTEN:
                    waitForSyn();
                    break;
                case ACK_SENDING:
                    sendingSynAck();
                    break;
                case SYN_REC:
                    waitForAck();
                    break;
                case ESTB:
                    System.out.println("Yay!!!!");
                    finishHandshake = true;
                    break;
            }
        }
    }

    private void changeStateToListen()
    {
        this.handshakeState = handshakeStates.LISTEN;
    }

    private void waitForSyn(){
        while(true) {
            try {
                byte[] buff = new byte[EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
                DatagramPacket data = new DatagramPacket(buff, buff.length);
                this.udp.setSoTimeout(Integer.MAX_VALUE);
                this.udp.receive(data);
                TCPPacket receivedPacket = new TCPPacket(data);
                if(!receivedPacket.getAckFlag() && receivedPacket.getSynFlag()){
                    this.destinationPort = data.getPort();
                    this.destinationIp = data.getAddress();
                    this.acknowledgmentNumber = receivedPacket.getSquenceNumber();
                    this.handshakeState = handshakeStates.ACK_SENDING;
                    this.udp.setSoTimeout(Config.TIMEOUT);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendingSynAck()
    {
        while (true) {
            try {
                TCPPacket sendPacket = new TCPPacket(
                        destinationIp.getHostAddress(),
                        destinationPort,
                        sequenceNumber,
                        acknowledgmentNumber + 1,
                        true,
                        true,
                        "");
                this.udp.send(sendPacket.getUDPPacket());
                this.handshakeState = handshakeStates.SYN_REC;
                break;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForAck()
    {
        while(true){
            try {
                byte[] buff = new byte[EnhancedDatagramSocket.DEFAULT_PAYLOAD_LIMIT_IN_BYTES];
                DatagramPacket data = new DatagramPacket(buff, buff.length);
                this.udp.receive(data);
                TCPPacket receivedPacket = new TCPPacket(data);
                if(receivedPacket.getAckFlag() && !receivedPacket.getSynFlag()
                    && receivedPacket.getAcknowledgmentNumber() == this.sequenceNumber + 1){
                    this.handshakeState = handshakeStates.ESTB;
                    break;
                }
            }
            catch (SocketTimeoutException timeoutException) {
                Log.serverHandshakeSynTimeout();
                this.handshakeState = handshakeStates.ACK_SENDING;
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
