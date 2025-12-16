package server;

public enum RTSPSates {

    /**
     * This is the initial state before any SETUP has been received
     */
    INIT,

    /**
     * This state indicates that the SETUP has been received and the server is ready to play
     */
    READY,

    /**
     * This state indicates that the media is currently being played
     */
    PLAYING,

    /**
     * This state indicates that the media is currently paused
     */
    PAUSED,

    /**
     * This state indicates that the media is currently recording
     */
    RECORDING,

    /**
     * This state indicates that the connection has been torn down
     */
    TEARDOWN
}
