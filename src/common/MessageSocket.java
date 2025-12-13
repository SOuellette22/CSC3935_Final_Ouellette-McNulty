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

        String temp;

        String stringMsg =
                recv.nextLine() + "\r\n" + // Header line
                recv.nextLine() + "\r\n"; // CSeq line

        Message msg = new Message(stringMsg);

        switch (msg.getType()) {
            case "OPTIONS": // Note: OPTIONS in spec is plural

                recv.nextLine(); // Read the empty line

                return new OptionsMessage(stringMsg);
            case "DESCRIBE":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Accept line

                recv.nextLine(); // Read the empty line

                return new DescribeMessage(stringMsg);
            case "SETUP":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Transport line

                recv.nextLine(); // Read the empty line

                return new SetUpMessage(stringMsg);
            case "PLAY":
            case "PAUSE":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Session line

                temp = recv.nextLine(); // Read the next line

                if (temp.startsWith("Range:")) { // Check for Range line
                    stringMsg += temp + "\r\n"; // Read the Range line if present

                    recv.nextLine(); // Read the empty line
                }

                return new PlayPauseMessage(stringMsg);
            case "RECORD":

                stringMsg += recv.nextLine() + "\r\n"; // Read the Session line

                stringMsg += recv.nextLine() + "\r\n"; // Read the Range line

                recv.nextLine();

                return new RecordMessage(stringMsg);
            case "TEARDOWN":
                stringMsg += recv.nextLine() + "\r\n"; // Read the Session line

                recv.nextLine(); // Read the empty line

                return new TeardownMessage(stringMsg);
            case "DATA":
                stringMsg += recv.nextLine() + "\r\n"; // Read the Sequence Number line

                stringMsg += recv.nextLine() + "\r\n"; // Read the Data line

                recv.nextLine(); // Read the empty line

                return new DataMessage(stringMsg);
            case "RTSP/1.0":

                temp = recv.nextLine();

                if (temp.startsWith("Session:")) { // Check for Session line
                    stringMsg += temp + "\r\n"; // Session line

                    if (recv.hasNextLine()) { // Check for Transport line
                        temp = recv.nextLine();
                        if (temp.startsWith("Transport:")) {
                            stringMsg += temp + "\r\n"; // Transport line
                        }
                    }

                    recv.nextLine(); // Read the empty line

                } else if (temp.startsWith("Public:")) { // Check for Public line
                    stringMsg += temp + "\r\n"; // Public line
                    recv.nextLine(); // Read the empty line

                } else if (temp.startsWith("Content-Type:")) { // Check for Content-Type line
                    stringMsg += temp + "\r\n"; // Content-Type line
                    int contentLength = Integer.parseInt(recv.nextLine().substring(16));
                    stringMsg += "Content-Length: " + contentLength + "\r\n";
                    stringMsg += "\r\n"; // Blank line
                    // Read content based on Content-Length
                    StringBuilder body = new StringBuilder();
                    for (int i = 0; i < contentLength; i++) {
                        body.append(recv.nextLine()).append("\n");
                    }
                    stringMsg += body + "\r\n";

                    recv.nextLine(); // Read the empty line
                }

                return new ServerResponse(stringMsg);
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

