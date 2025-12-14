package client;

import common.MessageSocket;
import common.messages.*;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * This is the client
 */
public class Client {

    private static boolean doHelp = false;
    private static String address = null;
    private static int serverPort = 5000; // default RTSP port
    private static int cseq = 1;
    private static int sessionID;

    /**
     * Prints the usage to the screen and exits.
     */
    public static void usage() {
        System.out.println("Usage: ");
        System.out.println("  client --server <addr>[:port]");
        System.out.println("  client --help");
        System.out.println("Options: ");
        System.out.printf("  %-15s %-20s\n", "-s, --server", "Specify the server address and optional port");
        System.out.printf("  %-15s %-20s\n", "-h, --help", "Display this help message");
        System.out.println("\nOnce connected, type commands:");
        System.out.println("  play      - Play an audio file");
        System.out.println("  pause     - Pause playback");
        System.out.println("  record    - Record audio");
        System.out.println("  teardown  - End session and exit");
        System.out.println("  help      - Show available commands");
    }

     /**
     * Processes the command line arugments.
     * @param args the command line arguments.
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
                    System.exit(1);
            }
        }

        if (doHelp) {
            usage();
            System.exit(0);
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
            System.exit(0);
        }

        processArgs(args);

        try (Socket socket = new Socket(address, serverPort);
             MessageSocket ms = new MessageSocket(socket);
             Scanner scan = new Scanner(System.in)) {

            System.out.println("Connected to " + address + ":" + serverPort);
            System.out.println("Type 'help' for commands. Type 'teardown' to exit.");

            boolean done = false;

            while (!done) {
                System.out.print("> ");
                String command = scan.nextLine().trim().toLowerCase();

                switch (command) {
                    case "play":
                        play(ms);
                        break;
                    case "pause":
                        pause(ms);
                        break;
                    case "record":
                        record(ms);
                        break;
                    case "teardown":
                        teardown(ms);
                        done = true;
                        break;
                    case "help":
                        System.out.println("Commands: play, pause, record, teardown, help");
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            }

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }
    //Message methods
    private static void play(MessageSocket ms) throws IOException {
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

        // For now, assume server hardcodes session ID
        sessionID = 123456;

        // PLAY
        Message play = new PlayPauseMessage("PLAY", address, cseq++, sessionID, "npt=0-");
        ms.sendMessage(play);
        System.out.println("Sent:\n" + play);
        resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void pause(MessageSocket ms) throws IOException {
        Message pause = new PlayPauseMessage("PAUSE", address, cseq++, sessionID);
        ms.sendMessage(pause);
        System.out.println("Sent:\n" + pause);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void record(MessageSocket ms) throws IOException {
        Message record = new RecordMessage(address, cseq++, sessionID, "npt=0-30");
        ms.sendMessage(record);
        System.out.println("Sent:\n" + record);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }

    private static void teardown(MessageSocket ms) throws IOException {
        Message teardown = new TeardownMessage(address, cseq++, sessionID);
        ms.sendMessage(teardown);
        System.out.println("Sent:\n" + teardown);
        Message resp = ms.getMessage();
        System.out.println("Received:\n" + resp);
    }
}
