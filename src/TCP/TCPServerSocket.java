public abstract class TCPServerSocket {
    public TCPServerSocket(int port) throws Exception {}

    public abstract void accept() throws Exception;

    public abstract void close() throws Exception;
}