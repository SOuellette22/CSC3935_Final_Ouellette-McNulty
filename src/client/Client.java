package client;

import common.MessageSocket;
import common.messages.OptionsMessage;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            OptionsMessage msg = new OptionsMessage("rtsp://localhost:5000", 1);
            System.out.println(msg);
            messageSocket.sendMessage(msg);

        } catch (Exception e) {
            System.err.println("Error establishing message socket: " + e.getMessage());
        }
    }
}