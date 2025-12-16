package client;

import common.MessageSocket;
import common.messages.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
            String port = ((ServerResponse) msg).getTransport().split("server_port=")[1];
            MessageSocket playbackSocket = new MessageSocket("localhost", Integer.parseInt(port));
            sessionId = ((ServerResponse) msg).getSessionId();
            System.out.println("Received message: \n" + msg);

            msg = new RecordMessage("rtsp://localhost/music/Audio1.wav", 2, sessionId, "AUDIO/PCM;rate=44100;channels=2;bits=16");
            messageSocket.sendMessage(msg);
            System.out.println("Sent message: \n" + msg);

            msg = messageSocket.getMessage();
            System.out.println("Received message: \n" + msg);

            SendSong sendSong = new SendSong(playbackSocket, new File("Audio1.wav"), 1234);
            sendSong.start();

//            Message msg = new DescribeMessage("rtsp://localhost/media.mp4", 1, "application/sdp");
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);
//
//            msg = new SetUpMessage("rtsp://localhost/media.mp4", 1, "RTP/UDP/TCP;unicast;client_port=8000-8001");
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            String port = ((ServerResponse) msg).getTransport().split("server_port=")[1];
//            MessageSocket playbackSocket = new MessageSocket("localhost", Integer.parseInt(port));
//            sessionId = ((ServerResponse) msg).getSessionId();
//            System.out.println("Received message: \n" + msg);
//
//            msg = new PlayPauseMessage("PLAY","rtsp://localhost/music/Audio1.wav", 3, sessionId);
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);
//
//            PlaySong playSong = new PlaySong(playbackSocket, sessionId);
//            playSong.start();
//            Thread.sleep(1000 * 10);
//
//            msg = new PlayPauseMessage("PAUSE","rtsp://localhost/music/Audio1.wav", 4, sessionId);
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);
//
//            playSong.pausePlayback();
//            Thread.sleep(1000 * 5);
//
//            msg = new PlayPauseMessage("PLAY","rtsp://localhost/music/Audio1.wav", 3, sessionId);
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);
//            playSong.pausePlayback();
//            Thread.sleep(1000 * 10);
//
//            msg = new TeardownMessage("rtsp://localhost/music/Audio1.wav", 5, sessionId);
//            messageSocket.sendMessage(msg);
//            System.out.println("Sent message: \n" + msg);
//
//            msg = messageSocket.getMessage();
//            System.out.println("Received message: \n" + msg);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}