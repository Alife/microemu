package javax.microedition.media;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.media.protocol.DataSource;

import org.microemu.android.media.AndroidMediaPlayer;
import org.microemu.android.media.MyMediaPlayer;

public class Manager {
    public static String MIDI_DEVICE_LOCATOR = "device://midi";
    public static String TONE_DEVICE_LOCATOR = "device://tone";
    
    public static Player createPlayer(InputStream inputStream, String type) {
        AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
        androidMediaPlayer.setInput(inputStream, type);
        return androidMediaPlayer;
    }
    
    public static Player createPlayer(String s) throws IOException {
//        AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
//        androidMediaPlayer.setInput(s, null);
//        return androidMediaPlayer;
    	MyMediaPlayer androidMediaPlayer = new MyMediaPlayer();
		androidMediaPlayer.setInput(s);
		return androidMediaPlayer;
    }
    
    public static Player createPlayer(DataSource dataSource) throws MediaException {
        String string = dataSource.getLocator();
        if (string != null) {
            if (string.startsWith("capture://video")) {
                string = "media.video.VideoCaptureImpl";
            } else if (string.startsWith(TONE_DEVICE_LOCATOR)) {
                string = "media.audio.MidiPlayerImpl";
            } else {
                if (string.startsWith(MIDI_DEVICE_LOCATOR)) {
                    string = "media.audio.MidiPlayerImpl";
                }
                string = null;
            }
        } else {
            string = null;
        }
        if (string == null) {
            dataSource.connect();
            String type = dataSource.getContentType();
            if (type != null) {
                type = type.toLowerCase();
                if (type.equals("audio/midi") || type.equals("audio/x-mid") || type.equals("audio/x-tone-seq")) {
                    type = "media.audio.MidiPlayerImpl";
                    string = type;
                } else if (type.startsWith("audio")) {
                    type = "media.audio.WavePlayerImpl";
                    string = type;
                } else {
                    if (type.startsWith("video")) {
                        type = "media.video.VideoPlayerImpl";
                        string = type;
                    }
                    type = string;
                    string = type;
                }
            } else {
                type = string;
                string = type;
            }
            if (type == null) {
                throw new MediaException("Unrecognized content type: " + string);
            }
        }
        throw new MediaException("No Player found for type. Not supported yet.");
    }
    
    public static String[] getSupportedContentTypes(String s) {
        return new String[] { "audio/*", "video/*", "audio/wav", "audio/x-tone-seq", "audio/x-wav", "audio/midi", "audio/x-midi", "audio/mpeg", "audio/amr", "audio/amr-wb", "audio/mp3", "audio/mp4", "video/mpeg", "video/mp4", "video/mpeg4", "video/3gpp" };
    }
    
    public static String[] getSupportedProtocols(String s) {
        return new String[] { "device", "file", "http" };
    }
    
    public static void playTone(int n, int n2, int n3) {
        // monitorenter(Manager.class)
        // monitorexit(Manager.class)
    }
}