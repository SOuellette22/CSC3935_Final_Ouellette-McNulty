package server;

import com.sun.jdi.event.ThreadDeathEvent;
import common.MessageSocket;
import common.messages.*;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.json.InvalidJSONException;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import merrimackutil.util.Tuple;
import merrimackutil.net.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

class Server {

    public static String configFile = "config.json";

    public static int port;
    public static String databaseDir;
    public static int maxConnections;
    public static String log;

    public static Log serverLog;

    /**
     * Prints the usage message for the server application
     */
    public static void usage() {
        System.out.println("Usage: ");
        System.out.println("  server");
        System.out.println("  server --config <file>");
        System.out.println("  server --help");
        System.out.println("Options: ");
        System.out.printf("  %-15s %-20s\n", "-c, --config", "Specify configuration file (default: config.json)");
        System.out.printf("  %-15s %-20s\n", "-h, --help", "Display this help message");
    }

    /**
     * Processes command-line arguments
     *
     * @param args the command-line arguments
     */
    public static void processArgs(String[] args) {
        OptionParser parser;

        LongOption[] opts = new LongOption[2];
        opts[0] = new LongOption("config", true, 'c');
        opts[1] = new LongOption("help", false, 'h');

        parser = new OptionParser(args);
        parser.setLongOpts(opts);
        parser.setOptString("c:h");

        Tuple<Character, String> currOpt;
        boolean doHelp = false;

        while (parser.getOptIdx() != args.length) {
            currOpt = parser.getLongOpt(false);
            switch (currOpt.getFirst()) {
                case 'c':
                    configFile = currOpt.getSecond();
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

        File file = new File(configFile);
        if (!file.exists() || file.length() == 0) {
            System.out.println("No valid config file provided!!!");
            System.exit(1);
        }

        try {
            deserialize(JsonIO.readObject(file));
        } catch (InvalidObjectException | FileNotFoundException | InvalidJSONException e) {
            System.err.println("Error reading config file!!!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length > 1)  {
            usage();
            System.exit(0);
        }

        processArgs(args);

        try {
            serverLog = new Log(log, "ServerLog");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        serverLog.log("Starting server on port " + port);
        serverLog.log("Using database directory: " + databaseDir);
        serverLog.log("Max connections set to: " + maxConnections);

        serverStart();

    }

    private static void serverStart() {

        try {

            ServerSocket serverSocket = new ServerSocket(port);
            ExecutorService pool = Executors.newFixedThreadPool(maxConnections);

            while(true) {

                MessageSocket messageSocket = new MessageSocket(serverSocket.accept());

                pool.execute(new ConnectionHandler(messageSocket, serverLog, databaseDir));

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for deserializing the JSON config file
     *
     * @param jsonType the JSONType to deserialize
     * @throws InvalidObjectException if the JSONType is not a valid JSONObject
     */
    private static void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Config file is not a valid JSON object");
        }

        JSONObject obj = (JSONObject) jsonType;

        obj.checkValidity(new String[]{"port", "db_dir", "max_connections"});

        if (obj.containsKey("log")) {
            log = obj.getString("log");
        }

        port = obj.getInt("port");
        databaseDir = obj.getString("db_dir");
        maxConnections = obj.getInt("max_connections");

    }
}