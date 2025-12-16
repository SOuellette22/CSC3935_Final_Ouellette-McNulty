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

    /**
     * Prints the usage message for the client application
     */
    public static void usage() {
        System.out.println("Usage:");
        System.out.println("  client --server <addr>[:port]");
        System.out.println("  client --help");
        System.out.println("Options:");
        System.out.println("  -s, --server   Server address and optional port");
        System.out.println("  -h, --help     Display this help message");
        System.exit(1);
    }

    /**
     * Processes command-line arguments
     */
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

    /**
     * Interactive CLI loop: stays connected until teardown is sent
     */
    public static void doCLI(MessageSocket ms) throws IOException {
        Scanner scan = new Scanner(System.in);
        boolean done = false;

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
                    break;
                case "play":
                    if (!setupComplete) {
                        System.out.println("You must SETUP before PLAY.");
                    } else {
                        System.out.print("Enter file to play: ");
                        String file = scan.nextLine().trim();
                        sendPlay(ms, file);
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
                    System.out.println("Recording requires a fresh session. Please TEARDOWN and SETUP again.");
                    break;
                case "teardown":
                    sendTeardown(ms);
                    done = true;
                    break;
                case "help":
                    System.out.println("Commands:");
                    System.out.println("  options   - query server capabilities");
                    System.out.println("  setup     - reserve transport for session");
                    System.out.println("  play      - play a file (requires setup)");
                    System.out.println("  pause     - pause playback (requires setup)");
                    System.out.println("  record    - record audio (requires new session)");
                    System.out.println("  teardown  - end session and exit");
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        }
    }

    private static void sendOptions(MessageSocket ms) throws IOException {
        Message options = new OptionsMessage(address, cseq++, "PLAY PAUSE TEARDOWN");
        ms.sendMessage(options);
        System.out.println("Sent:\n" + options);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void sendSetup(MessageSocket ms) throws IOException {
        Message setup = new SetUpMessage(address, cseq++, "RTP/AVP;unicast;client_port=8000-8001");
        ms.sendMessage(setup);
        System.out.println("Sent:\n" + setup);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
        // TODO: parse sessionID from resp instead of hardcoding
        sessionID = 123456;
    }

    private static void sendPlay(MessageSocket ms, String file) throws IOException {
        Message play = new PlayPauseMessage("PLAY", address + "/" + file, cseq++, sessionID, "npt=0-");
        ms.sendMessage(play);
        System.out.println("Sent:\n" + play);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void sendPause(MessageSocket ms) throws IOException {
        Message pause = new PlayPauseMessage("PAUSE", address, cseq++, sessionID);
        ms.sendMessage(pause);
        System.out.println("Sent:\n" + pause);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void sendTeardown(MessageSocket ms) throws IOException {
        Message teardown = new TeardownMessage(address, cseq++, sessionID);
        ms.sendMessage(teardown);
        System.out.println("Sent:\n" + teardown);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }
}
