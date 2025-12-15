package client;

public class SendSong extends Thread {

    private final String songName;
    private final byte[] songData;

    public SendSong(String songName, byte[] songData) {
        this.songName = songName;
        this.songData = songData;
    }

    @Override
    public void run() {
        // Logic to send the song to the server
        System.out.println("Sending song: " + songName);
        // Add actual song sending logic here
    }
}
