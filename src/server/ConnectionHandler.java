package server;

import common.MessageSocket;
import common.messages.Message;
import common.messages.OptionsMessage;
import common.messages.ServerResponse;
import common.messages.SetUpMessage;
import merrimackutil.net.Log;

import java.util.Random;

public class ConnectionHandler extends Thread {

    private MessageSocket socket;
    private Log logger;
    private RTSPSates state;

    private int sessionId;
    private Random r;

    public ConnectionHandler(MessageSocket socket, Log logger) {
        this.socket = socket;
        this.logger = logger;
        this.state = RTSPSates.INIT;
        this.r = new Random();
    }

    @Override
    public void run() {
        logger.log("Connection handler started.");

        while (state != RTSPSates.TEARDOWN) {
            // Handle connection based on current state
            Message msg = socket.getMessage();

            logger.log("INFO: Received " + msg.getType() + " message.");

            switch(msg.getType()) {
                case "OPTIONS" -> {

                    // Check if in valid state
                    if (state != RTSPSates.INIT) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setOptions("DESCRIBE, SETUP, PLAY, PAUSE, RECORD, TEARDOWN")
                            .build();
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent OPTIONS response.");
                }
                case "SETUP" -> {

                    // Check if in valid state
                    if (!(state == RTSPSates.INIT || state == RTSPSates.READY)) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    sessionId = r.nextInt(1000000 - 100000) + 100000; // Generate random session ID
                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .setTransport(((SetUpMessage) msg).getTransport() + ";server_port=9000-9001")
                            .build();
                    state = RTSPSates.READY;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent SETUP response.");
                }
                case "PLAY" -> {

                    // Check if in valid state
                    if (state != RTSPSates.READY) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .build();
                    state = RTSPSates.PLAYING;
                    socket.sendMessage(msg);

                    // TODO: Start streaming media data to client

                    logger.log("INFO: Sent PLAY response.");
                }
                case "PAUSE" -> {

                    // Check if in valid state
                    if (state != RTSPSates.PLAYING) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .build();
                    state = RTSPSates.READY;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent PAUSE response.");
                }
                default -> {
                    msg = new ServerResponse.ResponseBuilder(400, msg.getCseq())
                            .build();
                    socket.sendMessage(msg);
                    logger.log("ERROR: Unsupported method.");
                }
            }
        }
    }
}
