package tools;

public class Log {
    public static void serverHandshakeSynTimeout(){ System.out.println("Timeout in server for receiving Ack."); }
    public static void senderHandshakeAckTimeout() { System.out.println("Timeout in sender for receiving Ack."); }
    public static void listenForHandshake() {System.out.println("Wait for others to connect...");}
    public static void waitForGetHandshakeSyn() {System.out.println("Wait for get handshake syn in receiver...");}
    public static void handShakeSynReceived() {System.out.println("Handshake syn received.");}
    public static void handShakeSynAckSent() {System.out.println("Server sent Syn Ack...");}
    public static void handShakeAckReceived() {System.out.println("Server received Ack.");}
    public static void serverEstablished() {System.out.println("Server established successfully.");}
    public static void SenderGoingToSendData() { System.out.println("Sender going to handshake state."); }
    public static void changeStateToSynSending() { System.out.println("Sender change State To SynSending."); }
    public static void sendingSynToReceiver() { System.out.println("Sender Send Data to Receiver."); }
    public static void senderReceivedSynAckPacket() {  System.out.println("Sender receiving Syn Ack packet from receiver."); }
    public static void senderSendAckToReceiver() { System.out.println("Sender send Ack packet to receiver."); }
    public static void senderHandshakeFinished() { System.out.println("Sender handshaking finished."); }
}
