package client;

import common.MessageSocket;
import common.messages.DataMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The SendSong class is responsible for reading an audio file from disk,
 * splitting it into small chunks, encoding each chunk in Base64, and sending
 * the chunks to the server as RTSP DATA messages over a MessageSocket.
 *
 * It runs in its own thread to continuously transmit audio data until the
 * entire file has been sent, after which it sends an END message to signal
 * completion and closes the socket.
 */
public class SendSong extends Thread {

    private final File file;
    private final MessageSocket socket;
    private final int sessionID;

     /**
     * Constructs a new SendSong thread bound to a given session.
     *
     * @param socket    The MessageSocket used to send DATA messages to the server.
     * @param file      The audio file to be read and transmitted.
     * @param sessionID The RTSP session ID that identifies the recording session.
     */
    public SendSong(MessageSocket socket, File file, int sessionID) {
        this.socket = socket;
        this.file = file;
        this.sessionID = sessionID;
    }

    /**
     * Reads the audio file into memory, splits it into fixed-size chunks
     * (1764 bytes per chunk, corresponding to 10ms of stereo audio at 44.1 kHz),
     * encodes each chunk in Base64, and sends it to the server as a DataMessage.
     *
     * After all chunks have been sent, an END message is transmitted to mark
     * the end of the stream, and the socket is closed.
     *
     * @throws RuntimeException if an I/O error occurs while reading the file or sending messages.
     */
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
