package client;

import common.MessageSocket;
import common.messages.Message;
import common.messages.OptionsMessage;
import common.messages.PlayPauseMessage;
import common.messages.SetUpMessage;

import java.io.IOException;

class Client {

    public static void main(String[] args) {
        System.out.println("Client started.");

        try {
            MessageSocket messageSocket = new MessageSocket("localhost", 5000);

            Message msg = new OptionsMessage("localhost", 1);
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