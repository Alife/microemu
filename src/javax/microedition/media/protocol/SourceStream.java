package javax.microedition.media.protocol;

import javax.microedition.media.Controllable;

public interface SourceStream extends Controllable {
    public static final int NOT_SEEKABLE = 0;
    public static final int RANDOM_ACCESSIBLE = 2;
    public static final int SEEKABLE_TO_START = 1;

    public ContentDescriptor getContentDescriptor();

    public long getContentLength();

    public int getSeekType();

    public int getTransferSize();

    public int read(byte[] r1_byteA, int r2i, int r3i);

    public long seek(long r1j);

    public long tell();
}