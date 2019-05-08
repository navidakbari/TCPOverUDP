package tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ChunkMaker {
    private byte[] data;
    private int chunkSize;
    private int base;
    public ChunkMaker(String pathToFile, int chunkSize, int base)
    {
        this.base = base;
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
        return (( seqNum - base ) * chunkSize < data.length);
    }
    public byte[] getChunk(int seqNum)
    {
        if(!hasRemainingChunk(seqNum))
            return null;
        return Arrays.copyOfRange(data,
                ( seqNum - base) * chunkSize,
                ((seqNum - base + 1) * chunkSize) - 1 );
    }
}
