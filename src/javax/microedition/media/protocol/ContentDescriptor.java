package javax.microedition.media.protocol;

public class ContentDescriptor {
    private String x_a;

    public ContentDescriptor(String r1_String) {
        this.x_a = r1_String;
    }

    public String getContentType() {
        return this.x_a;
    }
}