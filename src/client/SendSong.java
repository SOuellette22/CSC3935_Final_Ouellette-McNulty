package client;

import common.MessageSocket;
import common.messages.DataMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SendSong extends Thread {

    private final File file;
    private final MessageSocket socket;
    private final int sessionID;

    public SendSong(MessageSocket socket, File file, int sessionID) {
        this.socket = socket;
        this.file = file;
        this.sessionID = sessionID;
    }

    @Override
    public void run() {

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            int chunkSize = 1764; // 44100 Hz * 2 bytes/sample * 2 channels * 0.01 sec = 1764 bytes for 10ms of audio
            int totalChunks = (int) Math.ceil((double) bytes.length / chunkSize); // Calculate total number of chunks

            // Send chunks
            for (int i = 0; i < totalChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, bytes.length);
                byte[] chunk = new byte[end - start];
                System.arraycopy(bytes, start, chunk, 0, end - start);

                String payload = java.util.Base64.getEncoder().encodeToString(chunk); // Encode chunk to Base64

                // Here you would create and send your DataMessage using the socket
                DataMessage dataMessage = new DataMessage("DATA", i, sessionID, payload);
                socket.sendMessage(dataMessage);
            }

            socket.sendMessage(new DataMessage("END", 0, sessionID, ""));
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
