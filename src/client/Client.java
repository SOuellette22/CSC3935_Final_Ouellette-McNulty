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

    /**
     * Prints the usage message for the client application
     */
    public static void usage() {
        System.out.println("Usage: ");
        System.out.println("  client --server <addr>[:port]");
        System.out.println("  client --help");
        System.out.println("Options: ");
        System.out.printf("  %-15s %-20s\n", "-s, --server", "Specify the server address and optional port");
        System.out.printf("  %-15s %-20s\n", "-h, --help", "Display this help message");
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

    /**
     * Interactive CLI loop: stays connected until teardown is sent
     */
    public static void doCLI(MessageSocket ms) throws IOException {
        String command;
        Scanner scan = new Scanner(System.in);
        boolean done = false;

        System.out.println("Connected to " + adress + ":" serverPort);
        System.out.println("Please type 'help' for help or 'teardown' to exit the application")

        while (!done) {
            do {
                Sysetm.out.print("> ");
                command = scan.nextLine();
                command = command.strip().toLowerCase();
            } while (command.equals(""));

            if (command.equals("teardown")) {
                teardown(ms);
                done = true;
            }
            else if (command.equals("plays")) {
                play(ms);
            }
            else if (command.equals("pause")) {
                pause(ms);
            }
            else if (command.equals("help")) {
                System.out.println();
                System.out.println("help\t\tdisplay this message.");
                System.out.println("play\t\tstart playback");
                System.out.println("pause\t\tpause playback");
                System.out.println("record\t\trecord audio");
                System.out.println("teardown\t\tend the session and exit")
            }
            else {
                System.out.println("Error: \"" + command + "\" unknown";)
            }
        }
    }

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

    /**
     * The entry point
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 2)
            usage();

        processArgs(args);

        try(Socket socket = new Socket(address, serverPort);
        MessageSocket ms = new MessageSocket(socket)) {
            doCLI(ms);
        } catch (IOException e) {
            System.err.println("client: " + e);
            System.exit(1);
        }

        System.exit(0);
    }
}

