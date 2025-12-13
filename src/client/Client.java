package client;

import common.MessageSocket;
import common.messages.*;

import java.io.IOException;

class Client {

    static int sessionId;

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            Message msg = new SetUpMessage("rtsp://localhost/media.mp4", 1, "RTP/UDP/TCP;unicast;client_port=8000-8001");
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            sessionId = ((ServerResponse) msg).getSessionId();
            System.out.println("Received message: \n" + msg);

            msg = new OptionsMessage("rtsp://localhost/media.mp4", 2);
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

            msg = new PlayPauseMessage("PLAY","rtsp://localhost/media.mp4", 3, sessionId);
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

            msg = new PlayPauseMessage("PAUSE","rtsp://localhost/media.mp4", 4, sessionId);
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}