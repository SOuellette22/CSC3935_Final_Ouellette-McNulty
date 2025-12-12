package common;

import common.messages.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Wraps socket class for use to send and receive DHT Messages
 */
public class MessageSocket extends Socket {

    // ----- PRIVATE FIELDS ----- //
    private Scanner recv;
    private PrintWriter send;

    /**
     * Creates Message Socket from a socket
     *
     * @note Useful for ServerSocket.accept();
     *
     * @throws IOException Throws if IOStreams cannot be established
     */
    public MessageSocket(Socket socket) throws IOException {
        super(); // Call parent class (Socket)

        try {
            this.recv = new Scanner(socket.getInputStream());
            this.send = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Message socket could not get IO streams setup");
            throw e; // Rethrow for consumer to handle
        }
    }

    /**
     * Creates Message Socket from address and port
     *
     * @throws IOException Throws if connection cannot be established
     */
    public MessageSocket(String addr, int port) throws IOException {
        super(addr, port); // Call parent class (Socket)

        try {
            this.recv = new Scanner(this.getInputStream());
            this.send = new PrintWriter(this.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Message socket could not get IO streams setup");
            throw e; // Rethrow for consumer to handle
        }
    }

    /**
     * This sends a message to the connected socket
     */
    public void sendMessage(Message msg) {
        send.println(msg);
    }

    /**
     * Receives a message from the connected socket.
     *
     * @return The received {@link Message} object.
     * @throws RuntimeException if the message type is unknown.
     */
    public Message getMessage() throws RuntimeException {

        String stringMsg =
                recv.nextLine() + "\r\n" + // Header line
                recv.nextLine() + "\r\n"; // CSeq line

        Message msg = new Message(stringMsg);

        switch (msg.getType()) {
            case "OPTIONS": // Note: OPTIONS in spec is plural

                recv.nextLine(); // Read the empty line

                return new OptionsMessage(stringMsg);
            case "SETUP":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Transport line

                recv.nextLine(); // Read the empty line

                return new SetUpMessage(stringMsg);
            case "PLAY":
            case "PAUSE":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Session line

                if (recv.hasNextLine()) {
                    stringMsg += recv.nextLine() + "\r\n"; // Read the Range line if present
                }

                recv.nextLine(); // Read the empty line

                return new PlayPauseMessage(stringMsg);
            default:
                throw new RuntimeException("Unknown message type: " + msg.getType());
        }

    }

    /**
     * Check if there is a message available to read
     *
     * @return true if a message is available, false otherwise
     */
    public boolean hasMessage() {
        return recv.hasNext();
    }


}

