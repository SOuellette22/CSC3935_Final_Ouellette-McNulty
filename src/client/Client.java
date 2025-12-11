package client;

import common.MessageSocket;
import common.messages.OptionMessage;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("127.0.0.1", 5000);

            OptionMessage msg = new OptionMessage("OPTIONS", "client1", 1, "DESCRIBE SETUP PLAY PAUSE TEARDOWN");
            System.out.println(msg);
            messageSocket.sendMessage(msg);

        } catch (Exception e) {
            System.err.println("Error establishing message socket: " + e.getMessage());
        }
    }
}