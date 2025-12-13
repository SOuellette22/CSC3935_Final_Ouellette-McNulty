package client;

import common.MessageSocket;
import common.messages.*;

import java.io.IOException;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            Message msg = new DataMessage("localhost", 1, 1234, "10101010010001010101");
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

            messageSocket.sendMessage(msg);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}