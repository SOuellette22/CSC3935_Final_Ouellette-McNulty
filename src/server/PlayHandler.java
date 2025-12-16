package server;

import common.MessageSocket;
import common.messages.DataMessage;
import merrimackutil.net.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The PlayHandler class is responsible for streaming audio data from the server
 * to a client over a MessageSocket. It reads an audio file from disk, splits it
 * into fixed-size chunks, encodes each chunk in Base64, and sends them as RTSP
 * DATA messages to the client.
 *
 * Playback runs in its own thread, supports pause/resume functionality, and
 * sends a final END message once the entire file has been transmitted.
 */
public class PlayHandler extends Thread {

    private boolean isPaused = false;
    private final MessageSocket socket;
    private final String filePath;
    private final Log logger;
    private final int sessionID;


    /**
     * Constructs a new PlayHandler thread bound to a given session.
     *
     * @param socket    The MessageSocket used to send DATA messages to the client.
     * @param filePath  The path to the audio file to be streamed.
     * @param logger    The logger used to record playback events.
     * @param sessionID The RTSP session ID that identifies the playback session.
     */
    public PlayHandler(MessageSocket socket, String filePath, Log logger, int sessionID) {
        this.filePath = filePath;
        this.socket = socket;
        this.logger = logger;
        this.sessionID = sessionID;
    }

    /**
     * Reads the audio file into memory, splits it into fixed-size chunks
     * (1764 bytes per chunk, corresponding to 10ms of stereo audio at 44.1 kHz),
     * encodes each chunk in Base64, and sends it to the client as a DataMessage.
     *
     * The method blocks while sending messages and supports pause/resume
     * functionality. After all chunks have been sent, an END message is
     * transmitted to mark the end of the stream, and playback is logged.
     *
     * @throws RuntimeException if an I/O error occurs while reading the file or sending messages.
     */
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

    /**
     * Toggles the pause state of playback. If playback is currently active,
     * calling this method will pause it; if paused, calling it will resume playback.
     *
     * This method uses synchronization to safely update the pause flag and notify
     * the playback thread to continue when resuming.
     */
    public synchronized void pausePlayback() {
        synchronized (this) {
            isPaused = !isPaused;
            this.notify();
        }
    }

}
