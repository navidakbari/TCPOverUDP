import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TCPSocket implements CongestionWindowPlotter {
    protected int port;
    protected String ip;
    public TCPSocket(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public abstract void send(String pathToFile , String destinationIp , int destinationPort) throws Exception;

    public final void sendAndLog(String pathToFile) throws Exception {
        Path filePath = Paths.get(pathToFile);
        String fileName = filePath.getFileName().toString();
        long fileSize = Files.size(filePath);

        System.err.println(
                String.format(
                        "Starting to send file \"%s\" with size of %d[byte] ...",
                        fileName,
                        fileSize
                )
        );

        long sendBeginTime = System.currentTimeMillis();
//        send(pathToFile);

        System.err.println(
                String.format(
                        "File \"%s\" with size of %d[byte] sent in %d[ms].",
                        fileName,
                        fileSize,
                        System.currentTimeMillis() - sendBeginTime
                )
        );
    }

    public abstract void receive(String pathToFile) throws Exception;

    public final void receiveAndLog(String pathToFile) throws Exception {
        Path filePath = Paths.get(pathToFile);
        String fileName = filePath.getFileName().toString();
        System.err.println(
                String.format(
                        "Starting to receive file \"%s\" ...",
                        fileName
                )
        );

        long receiveBeginTime = System.currentTimeMillis();
        receive(pathToFile);
        long receiveEndTime = System.currentTimeMillis();

        long fileSize = Files.size(filePath);
        System.err.println(
                String.format(
                        "File \"%s\" with size of %d[byte] received in %d[ms].",
                        fileName,
                        fileSize,
                        receiveEndTime - receiveBeginTime
                )
        );
    }

    public abstract void close() throws Exception;
}