package client;

import common.MessageSocket;
import common.messages.DataMessage;
import common.messages.Message;

import javax.sound.sampled.*;
import java.util.Base64;

public class PlaySong extends Thread {

    private final MessageSocket socket;
    private final int sessionID;
    private boolean isPaused = false;

    public PlaySong(MessageSocket socket, int sessionID) {
        this.socket = socket;
        this.sessionID = sessionID;
    }

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
