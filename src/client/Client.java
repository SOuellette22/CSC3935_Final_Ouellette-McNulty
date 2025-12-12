package client;

import common.MessageSocket;
import common.messages.Message;
import common.messages.OptionsMessage;
import common.messages.PlayPauseMessage;
import common.messages.SetUpMessage;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            Message msg = new PlayPauseMessage("PAUSE", "rtsp://localhost:5000", 2, 12345, "npt=0.000-");
            System.out.println("Sending message: \n" + msg);
            messageSocket.sendMessage(msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

            messageSocket.sendMessage(msg);

        } catch (Exception e) {
            System.err.println("Error establishing message socket: " + e.getMessage());
        }
    }
}