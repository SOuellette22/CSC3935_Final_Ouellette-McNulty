package common.messages;

public class OptionsMessage extends Message {

    private String options;

    /**
     * Constructor creates a new OPTIONS message from parameters
     */
    public OptionsMessage(String header, int cseq, String options) {
        super("OPTIONS", header, cseq);
        this.options = options;
    }

    public OptionsMessage(String header, int cseq) {
        super("OPTIONS", header, cseq);
    }

    /**
     * Constructor creates a new OPTIONS message from a message string
     */
    public OptionsMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("OPTIONS"))) {
            throw new IllegalArgumentException("Invalid message type for OptionMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Third line: Options: <options>
        if (lines.length == 3) {
            String[] optionsList = lines[2].split(" ");

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
            return super.toString() + "Public: " + options + "\r\n" + "\r";
        } else {
            return super.toString() + "\r";
        }
    }
}
