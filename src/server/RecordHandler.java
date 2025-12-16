package server;

import common.MessageSocket;
import common.messages.DataMessage;
import common.messages.Message;
import merrimackutil.net.Log;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.Base64;

public class RecordHandler extends Thread {

    private final MessageSocket socket;
    private final int sessionId;
    private final File file;
    private final Log logger;

    public RecordHandler(MessageSocket socket, int sessionId, File file, Log logger) {
        this.socket = socket;
        this.sessionId = sessionId;
        this.file = file;
        this.logger = logger;
    }

    @Override
    public void run() {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        Message msg = socket.getMessage();

        logger.log("INFO: Started recording.");

        int i = 0;

        // Loop to receive messages until "End" message is received
        while (!(msg.getHeader().equals("END"))) {

            // Process only Data messages
            if (msg.getHeader().equals("Data") && ((DataMessage) msg).getSessionID() == sessionId) {
                // Extract payload and decode from Base64
                String payload = ((DataMessage) msg).getPayload();
                byte[] chunk = Base64.getDecoder().decode(payload);

                // Append bytes to audioBytes list
                try {
                    byteStream.write(chunk);
                } catch (Exception e) {
                    logger.log("INFO: Error writing audio data: " + e.getMessage());
                }
            }
            msg = socket.getMessage();

            i++;

        }

        logger.log("INFO: Finished recording. Writing to WAV file...");

        // Convert ByteArrayOutputStream to byte array
        byte[] audioBytes = byteStream.toByteArray();

        // Define audio format (assuming 44.1kHz, 16-bit, stereo PCM)
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);

        // Create AudioInputStream from byte array
        InputStream bytesStream = new ByteArrayInputStream(audioBytes);
        long frameLength = audioBytes.length / audioFormat.getFrameSize();
        AudioInputStream audioInputStream = new AudioInputStream(bytesStream, audioFormat, frameLength);

        // Write the AudioInputStream to a WAV file
        try {
            // Write the AudioInputStream to a File in WAV format
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

            logger.log("INFO: WAV file written successfully: " + file.toPath());
        } catch (IllegalArgumentException | IOException e) {
            logger.log("ERROR: writing WAV file: " + e.getMessage());
        } finally {
            // Close the streams
            try {
                bytesStream.close();
                audioInputStream.close();
            } catch (IOException e) {
                logger.log("ERROR: closing streams: " + e.getMessage());
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.log("ERROR: closing socket: " + e.getMessage());
        }
    }
}
