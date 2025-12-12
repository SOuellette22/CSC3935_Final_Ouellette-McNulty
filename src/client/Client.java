package client;

import common.MessageSocket;
import common.messages.Message;
import common.messages.OptionsMessage;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            Message msg = new OptionsMessage("rtsp://localhost:5000", 1);
            System.out.println(msg);
            messageSocket.sendMessage(msg);

//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);

        } catch (Exception e) {
            System.err.println("Error establishing message socket: " + e.getMessage());
        }
    }
}