package server;

import common.MessageSocket;
import common.messages.*;
import merrimackutil.net.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Random;

public class ConnectionHandler extends Thread {

    private final MessageSocket socket;
    private final Log logger;
    private RTSPSates state;
    private final String databaseDir;
    private MessageSocket serverSocket;
    private PlayHandler playHandler;

    private int sessionId;
    private final Random r;

    public ConnectionHandler(MessageSocket socket, Log logger, String databaseDir) {
        this.socket = socket;
        this.logger = logger;
        this.databaseDir = databaseDir;
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
                    if (!(state == RTSPSates.INIT || state == RTSPSates.RECORDING)) {
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
                    int port = randomPort();
                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .setTransport(((SetUpMessage) msg).getTransport() + ";server_port="+port)
                            .build();
                    state = RTSPSates.READY;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent SETUP response.");

                    // Set up server socket for media streaming
                    setUpServerSocket(port);
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

                    int sessionIdMsg = ((PlayPauseMessage) msg).getSessionID();
                    String path = databaseDir + "/" + msg.getHeader().split("/",4)[3];

                    File file = new File(path);
                    if (!file.exists() || file.isDirectory()) {
                        msg = new ServerResponse.ResponseBuilder(404, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: File not found.");
                        break;
                    }

                    if (sessionIdMsg != sessionId) {
                        msg = new ServerResponse.ResponseBuilder(454, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Session ID mismatch.");
                        break;
                    }

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .build();
                    state = RTSPSates.PLAYING;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent PLAY response.");

                    logger.log("INFO: Playing file at path: " + path);

                    if (playHandler == null) {
                        playHandler = new PlayHandler(serverSocket, path, logger, sessionIdMsg);
                        playHandler.start();
                    } else {
                        playHandler.pausePlayback();
                    }
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

                    int sessionIdMsg = ((PlayPauseMessage) msg).getSessionID();
                    if (sessionIdMsg != sessionId) {
                        msg = new ServerResponse.ResponseBuilder(454, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Session ID mismatch.");
                        break;
                    }
                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .build();
                    state = RTSPSates.READY;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent PAUSE response.");

                    playHandler.pausePlayback();

                }
                case "RECORD" -> {

                    // Check if in valid state
                    if (state != RTSPSates.READY) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    int sessionIdMsg = ((RecordMessage) msg).getSessionID();
                    String path = msg.getHeader().split("/",4)[3];

                    if (sessionIdMsg != sessionId) {
                        msg = new ServerResponse.ResponseBuilder(454, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Session ID mismatch.");
                        break;
                    }

                    File file = new File(databaseDir + "/" + path);

                    if (file.exists() || file.isDirectory()) {
                        msg = new ServerResponse.ResponseBuilder(403, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: File already exists.");
                        break;
                    }

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setSessionId(sessionId)
                            .build();
                    state = RTSPSates.RECORDING;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent RECORD response.");

                    RecordHandler recordHandler = new RecordHandler(serverSocket, sessionIdMsg, file, logger);
                    recordHandler.start();

                }
                case "DESCRIBE" -> {

                    // Check if in valid state
                    if (state != RTSPSates.INIT) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    String sdpInfo = "v=0\n" +
                            "o=- 1 1 IN IP4 127.0.0.1\n" +
                            "s=Stereo PCM Audio\n" +
                            "t=0 0\n" +
                            "m=audio 0 RTP/AVP 96\n" +
                            "a=rtpmap:96 L16/44100/2";

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .setContentType("application/sdp")
                            .setContentLength(sdpInfo.length())
                            .setBody(sdpInfo)
                            .build();
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent DESCRIBE response.");
                }
                case "TEARDOWN" -> {
                    // Check if in valid state
                    if (state == RTSPSates.INIT) {
                        msg = new ServerResponse.ResponseBuilder(455, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Method not valid in current state.");
                        break;
                    }

                    int sessionIdMsg = ((TeardownMessage) msg).getSessionID();
                    if (sessionIdMsg != sessionId) {
                        msg = new ServerResponse.ResponseBuilder(454, msg.getCseq())
                                .build();
                        socket.sendMessage(msg);
                        logger.log("ERROR: Session ID mismatch.");
                        break;
                    }

                    playHandler.pausePlayback();
                    playHandler = null;

                    msg = new ServerResponse.ResponseBuilder(200, msg.getCseq())
                            .build();
                    state = RTSPSates.TEARDOWN;
                    socket.sendMessage(msg);
                    logger.log("INFO: Sent TEARDOWN response.");
                }
                default -> {
                    msg = new ServerResponse.ResponseBuilder(400, msg.getCseq())
                            .build();
                    socket.sendMessage(msg);
                    logger.log("ERROR: Unsupported method.");
                }
            }
        }

        logger.log("Connection handler terminating.");

         // Close sockets
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allocates a random available port on the server.
     *
     * @return An available port number.
     */
    private int randomPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            logger.log("ERROR: Unable to allocate random port.");
            return -1;
        }
    }

    /**
     * Sets up a server socket to listen for media streaming on the specified port.
     *
     * @param port The port number to listen on.
     */
    private void setUpServerSocket(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            this.serverSocket = new MessageSocket(serverSocket.accept());
        } catch (IOException e) {
            logger.log("ERROR: Unable to set up server socket on port " + port);
            throw new RuntimeException(e);
        }
    }
}
