package common;

import common.messages.Message;
import common.messages.OptionMessage;
import common.messages.PlayPauseMessage;
import common.messages.SetUpMessage;

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

        if (!recv.hasNext()) {
            return null; // No message to read
        }

        StringBuilder message = new StringBuilder();

        while (recv.hasNext()) {
            String line = recv.nextLine();

            if (line.isEmpty()) {
                recv.nextLine();
                break; // End of message
            }

            message.append(line);

            if (recv.hasNext()) {
                message.append("\r\n");
            }
        }

        Message msg = new Message(message.toString());

        switch (msg.getType()) {
            case "OPTION": // Note: OPTIONS in spec is plural
                return new OptionMessage(message.toString());
            case "SETUP":
                return new SetUpMessage(message.toString());
            case "PLAY":
            case "PAUSE":
                return new PlayPauseMessage(message.toString());
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

