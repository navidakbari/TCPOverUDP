package tools;

public class Log {
    public static void serverHandshakeSynTimeout(){
        System.out.println("Timeout in server for receiving Ack.");
    }
    public static void senderHandshakeAckTimeout() { System.out.println("Timeout in sender for receiving Ack."); }
}
