package client;

import common.MessageSocket;
import common.messages.DataMessage;
import common.messages.Message;

import javax.sound.sampled.*;
import java.util.Base64;

/**
 * The PlaySong class is responsible for receiving audio data messages from the server
 * over a MessageSocket and playing them back using Java's audio system.
 * 
 * It runs in its own thread to continuously process incoming DATA messages,
 * decode their Base64 payloads into raw audio bytes, and stream them to a SourceDataLine.
 * 
 * Playback can be paused and resumed by toggling the pause state with {@link #pausePlayback()}.
 */
public class PlaySong extends Thread {

    private final MessageSocket socket;
    private final int sessionID;
    private boolean isPaused = false;

    /**
     * Constructs a new PlaySong thread bound to a given session.
     *
     * @param socket    The MessageSocket used to receive DATA messages from the server.
     * @param sessionID The RTSP session ID that identifies the playback session.
     */
    public PlaySong(MessageSocket socket, int sessionID) {
        this.socket = socket;
        this.sessionID = sessionID;
    }

    /**
     * Continuously receives DATA messages from the server, decodes their Base64 payloads,
     * and writes the audio data to a SourceDataLine for playback.
     * 
     * The method blocks while waiting for messages and terminates when an "End" header
     * is received or when playback completes. Handles pause/resume functionality by
     * waiting when {@code isPaused} is true.
     */
    @Override
    public void run() {

        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            // Starts the audio stream
            line.open(audioFormat);

            line.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    System.out.println("Audio playback completed.");
                    line.close();
                }
            });

            line.start();

            Message msg = socket.getMessage();

            while (msg.getType().equals("DATA") && !msg.getHeader().equals("End")) {
                while (isPaused) {
                    synchronized (this) {
                        this.wait();
                    }

                }

                if (!(((DataMessage) msg).getSessionID() == sessionID)) {
                    // Get the next message from the socket
                    msg = socket.getMessage(); // blocking call to receive the next message
                    continue;
                }

                String payload = ((DataMessage) msg).getPayload();

                // Process the data message
                byte[] audioData = Base64.getDecoder().decode(payload); // decode the payload from base 64 encoding

                // Write the audio data to the line
                line.write(audioData, 0, audioData.length);

                // Get the next message from the socket
                msg = socket.getMessage(); // blocking call to receive the next message
            }

            // Stop the line when the audio is finished
            line.drain();
            line.stop();
            line.close();

        } catch (LineUnavailableException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized void pausePlayback() {
        synchronized (this) {
            isPaused = !isPaused;
            this.notify();
        }
    }
}
