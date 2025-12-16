package client;

import common.MessageSocket;
import common.messages.*;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static boolean doHelp = false;
    private static String address = null; // server address
    private static int serverPort = 5000; // default RTSP port
    private static int cseq = 1;
    private static int sessionID;
    private static boolean setupComplete = false;
    private static MessageSocket playbackSocket = null;
    private static PlaySong player = null;
    private static String playingFile = "";

    public static void usage() {
        System.out.println("Usage:");
        System.out.println("  client --server <addr>[:port]");
        System.out.println("  client --help");
        System.out.println("Options:");
        System.out.println("  -s, --server   Server address and optional port");
        System.out.println("  -h, --help     Display this help message");
        System.exit(1);
    }

    public static void processArgs(String[] args) {
        OptionParser parser;

        LongOption[] opts = new LongOption[2];
        opts[0] = new LongOption("server", true, 's');
        opts[1] = new LongOption("help", false, 'h');

        parser = new OptionParser(args);
        parser.setLongOpts(opts);
        parser.setOptString("s:h");

        Tuple<Character, String> currOpt;

        while (parser.getOptIdx() != args.length) {
            currOpt = parser.getLongOpt(false);
            switch (currOpt.getFirst()) {
                case 's':
                    parseServer(currOpt.getSecond());
                    break;
                case 'h':
                    doHelp = true;
                    break;
                default:
                    System.out.println("Invalid option " + currOpt.getFirst());
                    usage();
            }
        }

        if (doHelp) {
            usage();
        }

        if (address == null) {
            System.out.println("Missing server address.");
            usage();
        }
    }

    private static void parseServer(String arg) {
        if (arg.contains(":")) {
            String[] parts = arg.split(":");
            address = parts[0];
            serverPort = Integer.parseInt(parts[1]);
        } else {
            address = arg;
        }
    }

    public static void doCLI(MessageSocket ms) throws IOException {
        Scanner scan = new Scanner(System.in);
        boolean done = false;
        boolean playStarted = false;   // track if PLAY has been issued

        System.out.println("Connected to " + address + ":" + serverPort);
        System.out.println("Type 'help' for commands. Type 'teardown' to exit.");

        while (!done) {
            System.out.print("> ");
            String command = scan.nextLine().trim().toLowerCase();

            switch (command) {
                case "options":
                    sendOptions(ms);
                    break;
                case "setup":
                    sendSetup(ms);
                    setupComplete = true;
                    playStarted = false; // reset when starting a new session
                    break;
                case "play":
                    if (!setupComplete) {
                        System.out.println("You must SETUP before PLAY.");
                    } else {
                        if (player == null) {
                            System.out.print("Enter file to play: ");
                            playingFile = scan.nextLine().trim();
                        }
                        sendPlay(ms, playingFile);
                        playStarted = true;
                    }
                    break;
                case "pause":
                    if (!setupComplete) {
                        System.out.println("You must SETUP before PAUSE.");
                    } else {
                        sendPause(ms);
                    }
                    break;
                case "record":
                    if (!setupComplete) {
                        System.out.println("You must SETUP before RECORD.");
                    } else if (playStarted) {
                        System.out.println("Cannot RECORD after PLAY has started. Please TEARDOWN and SETUP again.");
                    } else {
                        System.out.print("Enter file path to record: ");
                        String filePath = scan.nextLine().trim();
                        sendRecord(ms, filePath);
                    }
                    break;
                case "teardown":
                    sendTeardown(ms);
                    setupComplete = false;
                    playStarted = false;
                    done = true;
                    break;
                case "help":
                    System.out.println("Commands:");
                    System.out.println("  options   - query server capabilities");
                    System.out.println("  setup     - reserve transport for session");
                    System.out.println("  play      - play a file (requires setup)");
                    System.out.println("  pause     - pause playback (requires setup)");
                    System.out.println("  record    - record audio (requires setup, only before play)");
                    System.out.println("  teardown  - end session and exit");
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        }
    }


    private static void sendOptions(MessageSocket ms) throws IOException {
        Message options = new OptionsMessage("rtsp://" + address + ":" + serverPort, cseq++);
        ms.sendMessage(options);
        Message resp = ms.getMessage();
        if (resp instanceof ServerResponse respServer) {
            System.out.println("Server Capabilities:\n" + respServer.getOptions());
        } else {
            System.out.println("Received Bad Message:\n" + resp);
        }
    }

    private static void sendDescribe(MessageSocket ms) throws IOException {
        Message describe = new DescribeMessage("rtsp://" + address + ":" + serverPort, cseq++, "application/sdp");
        ms.sendMessage(describe);
        Message resp = ms.getMessage();
        if (resp instanceof ServerResponse respServer) {
            if (respServer.getCode() != 200) {
                System.out.println("Failed to get description. Server response:\n" + respServer.getMessage());
                return;
            }
            System.out.println("Description:\n" + respServer.getContentType() + "\n" + respServer.getBody());
        } else {
            System.out.println("Received Bad Message:\n" + resp);
        }
    }

    private static void sendSetup(MessageSocket ms) throws IOException {
        Message setup = new SetUpMessage("rtsp://" + address + ":" + serverPort, cseq++, "RTP/AVP;unicast;client_port=8000-8001");
        ms.sendMessage(setup);

        Message resp = ms.getMessage();

        if (resp instanceof ServerResponse serverResp) {
            if (serverResp.getCode() != 200) {
                System.out.println("Failed to setup session. Server response:\n" + serverResp.getMessage());
                return;
            }
            sessionID = serverResp.getSessionId();   // <-- capture session ID
            String port = serverResp.getTransport().split("server_port=")[1];
            playbackSocket = new MessageSocket(address, Integer.parseInt(port));
            System.out.println("Setup was Successful");
        } else {
            System.out.println("Warning: SETUP response did not include a session ID.");
        }
    }

    private static void sendPlay(MessageSocket ms, String file) throws IOException {
        Message play = new PlayPauseMessage("PLAY", "rtsp://" + address + ":" + serverPort + "/" + file, cseq++, sessionID);
        ms.sendMessage(play);
        Message resp = ms.getMessage();

        if (resp instanceof ServerResponse serverResp) {
            if (serverResp.getCode() == 200) {
                if (player != null) {
                    System.out.println("Resume Playing");
                    player.pausePlayback();
                } else {
                    System.out.println("Playback started for file: " + file);
                    player = new PlaySong(playbackSocket, sessionID);
                    player.start();
                }
            } else {
                System.out.println("Failed to start playback. Server response:\n" + serverResp.getMessage());
            }
        } else {
            System.out.println("Received Bad Message:\n" + resp);
        }
    }

    private static void sendPause(MessageSocket ms) throws IOException {
        Message pause = new PlayPauseMessage("PAUSE", "rtsp://" + address + ":" + serverPort, cseq++, sessionID);
        ms.sendMessage(pause);
        Message resp = ms.getMessage();

        if (resp instanceof ServerResponse serverResp) {
            if (serverResp.getCode() == 200) {
                if (player != null) {
                    player.pausePlayback();
                    System.out.println("Playback paused.");
                }
            } else {
                System.out.println("Failed to start playback. Server response:\n" + serverResp.getMessage());
            }
        } else {
            System.out.println("Received Bad Message:\n" + resp);
        }

    }

    private static void sendTeardown(MessageSocket ms) throws IOException {
        Message teardown = new TeardownMessage("rtsp://" + address + ":" + serverPort, cseq++, sessionID);
        ms.sendMessage(teardown);

        Message resp = ms.getMessage();

        if (resp instanceof ServerResponse serverResp) {
            if (serverResp.getCode() == 200) {
                System.out.println("Session torn down successfully.");
                player.pausePlayback();
                player = null;
                playbackSocket.close();
            } else {
                System.out.println("Failed to teardown session. Server response:\n" + serverResp.getMessage());
            }
        } else {
            System.out.println("Received Bad Message:\n" + resp);
        }
    }

    private static void sendRecord(MessageSocket ms, String filePath) throws IOException {
        // Construct a RECORD message with the file path included in the header
        Message record = new RecordMessage("rtsp://" + address + ":" + serverPort + "/" + filePath, cseq++, sessionID, "npt=0-30");
        ms.sendMessage(record);
        System.out.println("Sent:\n" + record);

        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
        }

        processArgs(args);

        try (Socket socket = new Socket(address, serverPort);
             MessageSocket ms = new MessageSocket(socket)) {

            doCLI(ms);

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }
}
