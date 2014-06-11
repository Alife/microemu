package javax.microedition.media.protocol;

import javax.microedition.media.Control;
import javax.microedition.media.Controllable;

public abstract class DataSource implements Controllable {
    private String x_a;

    public DataSource(String r1_String) {
        this.x_a = r1_String;
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract String getContentType();

    public abstract Control getControl(String r1_String);

    public abstract Control[] getControls();

    public String getLocator() {
        return this.x_a;
    }

    public abstract SourceStream[] getStreams();

    public abstract void start();

    public abstract void stop();
}