package client;

import common.MessageSocket;
import common.messages.*;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private static boolean doPlay = false;
    private static boolean doPause = false;
    private static boolean doRecord = false;
    private static boolean doHelp = false;

    private static String address = null; // server address
    private static int serverPort = 5000; // default RTSP port
    private static int cseq = 1;
    private static int sessionID;

    /**
     * Prints the usage message for the client application
     */
    public static void usage() {
        System.out.println("Usage: ");
        System.out.println("  client --play <server>[:port]");
        System.out.println("  client --pause <server>[:port]");
        System.out.println("  client --record <server>[:port]");
        System.out.println("  client --help");
        System.out.println("Options: ");
        System.out.printf("  %-15s %-20s\n", "-p, --play", "Play an audio file from the server");
        System.out.printf("  %-15s %-20s\n", "-u, --pause", "Pause the current audio stream");
        System.out.printf("  %-15s %-20s\n", "-r, --record", "Record an audio file on the server");
        System.out.printf("  %-15s %-20s\n", "-h, --help", "Display this help message");
    }

    /**
     * Processes command-line arguments
     */
    public static void processArgs(String[] args) {
        OptionParser parser;

        LongOption[] opts = new LongOption[4];
        opts[0] = new LongOption("play", true, 'p');
        opts[1] = new LongOption("pause", true, 'u');
        opts[2] = new LongOption("record", true, 'r');
        opts[3] = new LongOption("help", false, 'h');

        parser = new OptionParser(args);
        parser.setLongOpts(opts);
        parser.setOptString("p:u:r:h");

        Tuple<Character, String> currOpt;

        while (parser.getOptIdx() != args.length) {
            currOpt = parser.getLongOpt(false);
            switch (currOpt.getFirst()) {
                case 'p':
                    doPlay = true;
                    parseServer(currOpt.getSecond());
                    break;
                case 'u':
                    doPause = true;
                    parseServer(currOpt.getSecond());
                    break;
                case 'r':
                    doRecord = true;
                    parseServer(currOpt.getSecond());
                    break;
                case 'h':
                    doHelp = true;
                    break;
                default:
                    System.out.println("Invalid option " + currOpt.getFirst());
                    usage();
                    System.exit(1);
            }
        }

        if (doHelp) {
            usage();
            System.exit(0);
        }

        if ((doPlay || doPause || doRecord) && address == null) {
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

    // Main 
    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            System.exit(0);
        }

        processArgs(args);

        try {
            if (doPlay) {
                play();
            } else if (doPause) {
                // pause();
            } else if (doRecord) {
                // record();
            }
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }

    private static void play() throws IOException {
        try (Socket socket = new Socket(address, serverPort);
             MessageSocket ms = new MessageSocket(socket)) {

            // OPTIONS
            Message options = new OptionsMessage(address, cseq++);
            ms.sendMessage(options);
            System.out.println("Sent:\n" + options);
            Message resp = ms.getMessage();
            System.out.println("Received:\n" + resp);

            // SETUP
            Message setup = new SetUpMessage(address, cseq++, "RTP/AVP;unicast;client_port=8000-8001");
            ms.sendMessage(setup);
            System.out.println("Sent:\n" + setup);
            resp = ms.getMessage();
            System.out.println("Received:\n" + resp);

            // For now server hardcodes session ID
            sessionID = 123456;

            // PLAY
            Message play = new PlayPauseMessage("PLAY", address, cseq++, sessionID, "npt=0-");
            ms.sendMessage(play);
            System.out.println("Sent:\n" + play);
            resp = ms.getMessage();
            System.out.println("Received:\n" + resp);
        }
    }

    // Future methods:
    // private static void pause()
    // private static void record()
}
