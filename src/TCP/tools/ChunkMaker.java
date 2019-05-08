package tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ChunkMaker {
    private byte[] data;
    private int chunkSize;
    public ChunkMaker(String pathToFile, int chunkSize)
    {
        this.chunkSize = chunkSize;
        readDataFromFile(pathToFile);
    }
    private void readDataFromFile(String pathToFile) {
        try {
            data = Files.readAllBytes(Paths.get(pathToFile));
            System.out.println(Arrays.toString(data));
        } catch (IOException e) {
            e.printStackTrace();
            Log.errorInReadingFile();
        }
    }
    public boolean hasRemainingChunk(int seqNum)
    {
        return (seqNum * chunkSize < data.length);
    }
    public byte[] getChunk(int seqNum)
    {
        if(!hasRemainingChunk(seqNum))
            return null;
        return Arrays.copyOfRange(data,
                seqNum * chunkSize,
                ((seqNum + 1) * chunkSize) - 1 );
    }
}
