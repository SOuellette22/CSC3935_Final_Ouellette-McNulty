package server;

import common.MessageSocket;
import common.messages.DataMessage;
import merrimackutil.net.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

public class PlayHandler extends Thread {

    private boolean isPaused = false;
    private final MessageSocket socket;
    private final String filePath;
    private final Log logger;
    private final int sessionID;


    public PlayHandler(MessageSocket socket, String filePath, Log logger, int sessionID) {
        this.filePath = filePath;
        this.socket = socket;
        this.logger = logger;
        this.sessionID = sessionID;
    }

    @Override
    public void run() {

        logger.log("INFO: Started Playing song to client.");

        int i = 0; // Chunk index

        try {
            File audioFile = new File(filePath);

            byte[] bytes = Files.readAllBytes(audioFile.toPath());
            int chunkSize = 1764; // 44100 Hz * 2 bytes/sample * 2 channels * 0.01 sec = 1764 bytes for 10ms of audio
            int totalChunks = (int) Math.ceil((double) bytes.length / chunkSize);

            for (i = 0; i < totalChunks; i++) {
                while (isPaused) {
                    synchronized (this) {
                        this.wait();
                    }

                }

                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, bytes.length);
                byte[] chunk = new byte[end - start];
                System.arraycopy(bytes, start, chunk, 0, end - start);

                String payload = java.util.Base64.getEncoder().encodeToString(chunk);
                DataMessage dataMessage = new DataMessage("DATA", i, sessionID, payload);
                socket.sendMessage(dataMessage);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        DataMessage endMessage = new DataMessage("End", i, sessionID, "");
        socket.sendMessage(endMessage);
        logger.log("INFO: Finished Playing song to client.");
    }

    public synchronized void pausePlayback() {
        synchronized (this) {
            isPaused = !isPaused;
            this.notify();
        }
    }

}
