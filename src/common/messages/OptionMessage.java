package common.messages;

import java.util.Objects;

public class OptionMessage extends Message {

    private String options;

    /**
     * Constructor creates a new OPTIONS message from parameters
     */
    public OptionMessage(String header, int cseq, String options) {
        super("OPTION", header, cseq);
        this.options = options;
    }

    /**
     * Constructor creates a new OPTIONS message from a message string
     */
    public OptionMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("OPTION"))) {
            throw new IllegalArgumentException("Invalid message type for OptionMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Third line: Options: <options>
        String[] optionsList = lines[1].split(" ");

        // Reconstruct options string
        StringBuilder optionsBuilder = new StringBuilder();
        for (int i = 1; i < optionsList.length; i++) {
            optionsBuilder.append(optionsList[i]);
            if (i < optionsList.length - 1) {
                optionsBuilder.append(" ");
            }
        }

        this.options = optionsBuilder.toString();
    }

    /**
     * Get options
     *
     * @return String of options
     */
    public String getOptions() {
        return options;
    }

    /**
     * Set options
     *
     * @param options String of options
     */
    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * Override toString to include options
     */
    @Override
    public String toString() {
        if (options != null && !options.isEmpty()) {
            return super.toString() + "Options: " + options + "\r\n" + "\r\n";
        } else {
            return super.toString() + "\r\n";
        }
    }
}
