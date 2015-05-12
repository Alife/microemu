package org.microemu.android.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Vector;

import javax.microedition.media.Control;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

import org.microemu.midp.media.TimeBase;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class AndroidMediaPlayer implements Player, VolumeControl
{
    private static Vector<AndroidMediaPlayer> x_j=new Vector<AndroidMediaPlayer>();;
    protected long desiredTime = -1L;
    protected int loopCount = 1;
    protected int state = 100;
    MediaPlayer mediaPlayer;
    private AndroidMediaPlayer.MediaListener mListener;
    private TimeBase timeBase;
    private Vector<PlayerListener> listeners = new Vector<PlayerListener>();
    private Context context;
    private String type;
    private String x_g;
    private String x_h;
    private String x_i;
    private int x_k = 100;
    private boolean x_l= false;
    
    public AndroidMediaPlayer() {
        super();
        mediaPlayer = new MediaPlayer();
        mListener = new MediaListener();
        mediaPlayer.setOnCompletionListener((OnCompletionListener)mListener);
        mediaPlayer.setOnBufferingUpdateListener((OnBufferingUpdateListener)mListener);
        mediaPlayer.setOnErrorListener((OnErrorListener)mListener);
        mediaPlayer.setOnPreparedListener((OnPreparedListener)mListener);
        mediaPlayer.setOnSeekCompleteListener((OnSeekCompleteListener)mListener);
        x_j.addElement(this);
    }
    public AndroidMediaPlayer(Context context) {
        super();
        this.context = context;
        mediaPlayer = new MediaPlayer();
        mListener = new MediaListener();
        mediaPlayer.setOnCompletionListener((OnCompletionListener)mListener);
        mediaPlayer.setOnBufferingUpdateListener((OnBufferingUpdateListener)mListener);
        mediaPlayer.setOnErrorListener((OnErrorListener)mListener);
        mediaPlayer.setOnPreparedListener((OnPreparedListener)mListener);
        mediaPlayer.setOnSeekCompleteListener((OnSeekCompleteListener)mListener);
        x_j.addElement(this);
    }
    
	interface IInputStreamToServerSocket
	{
	    String getUri(InputStream p0);
	}
	class xa implements IInputStreamToServerSocket
	{
	    private int int1=1;
	    private String string;
	    
	    public xa(Context context) {
	        super();
	        File file = new File(getCacheDir(context));
	        if (!file.exists()) {
	            file.mkdirs();
	        }
	    }
	    
	    public String x_a(Context context) {
	        StringBuilder append = new StringBuilder()
		        .append(getCacheDir(context)).append("/")
		        //.append(Process.myPid())
		        .append(".");
	        return append.append(int1).toString();
	    }
	    
	    private String getCacheDir(Context context) {
	        File file = new File("/sdcard");
	        if (file.exists() && file.canWrite()) {
	            return "/sdcard/" + context.getPackageName() + "/cache";
	        }
	        return context.getCacheDir().getAbsolutePath();
	    }
	    
	    @Override
	    public String getUri(InputStream inputStream) {
	        File file = null;
	        FileOutputStream fileOutputStream = null;
	        try {
	            String x_b = string;
	            StringBuilder append = new StringBuilder()
		            //.append("").append(Process.myPid())
		            .append(".");
	            int1 = int1 + 1;
	            file = new File(x_b, append.append(int1).toString());
	            fileOutputStream = new FileOutputStream(file);
	            int n = 4096;
	            IOUtil.copyStream(inputStream, fileOutputStream, n);
				inputStream.close();
	            fileOutputStream.close();
	            return file.getAbsolutePath();
	        }catch (IOException e) {
				e.printStackTrace();
			}
	        return null;
	    }
	}
	public class InputStreamToServerSocket implements IInputStreamToServerSocket {
        private int x_a;

        public InputStreamToServerSocket(AndroidMediaPlayer r1_AndroidMediaPlayer, int r2i) {
            x_a = r2i;
        }

        public ServerSocket getUnusedServerSocket(int r3i, int r4i) {
            ServerSocket r1_ServerSocket = null;
            while (true) {
                ServerSocket r0_ServerSocket;
                try {
                    r0_ServerSocket = new ServerSocket();
                    r0_ServerSocket.bind(new InetSocketAddress(r3i));
                } catch (IOException e) {
                    r0_ServerSocket = r1_ServerSocket;
                }
                r4i--;
                r3i++;
                if (r0_ServerSocket != null || r4i <= 0) {
                    return r0_ServerSocket;
                }
                r1_ServerSocket = r0_ServerSocket;
            }
        }

        public String getUri(InputStream r4_InputStream) {
            ServerSocket r0_ServerSocket = getUnusedServerSocket(x_a, 5);
            if (r0_ServerSocket == null) {
                //throw new IOException("no unused server socket");
            	return null;
            } else {
                //new x_c(this, r0_ServerSocket, r4_InputStream).start();
                return "socket://localhost:" + r0_ServerSocket.getLocalPort();
            }
        }
    }

    public class MediaListener implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener {
        public void onBufferingUpdate(MediaPlayer r1_MediaPlayer, int r2i) {
        }

        public void onCompletion(MediaPlayer mediaPlayer) {
            loopCount--;
            notifyListeners(PlayerListener.END_OF_MEDIA, new Long((long) (mediaPlayer.getCurrentPosition() * 1000)));
            if (loopCount <= 0 || state != 400) {
                state = 300;
            } else {
            	mediaPlayer.seekTo(0);
                start();
            }
        }

        public boolean onError(MediaPlayer r4_MediaPlayer, int r5i, int r6i) {
            notifyListeners(PlayerListener.ERROR, "Error:what=" + r5i + ",extra=" + r6i);
            return false;
        }

        public void onPrepared(MediaPlayer r1_MediaPlayer) {
        }

        public void onSeekComplete(MediaPlayer r1_MediaPlayer) {
        }
    }

    public class PlayerNotifier implements Runnable {
        public void run() {
            notifyListeners(null, null);
        }
    }

    public static void terminatePlayers() {
        while (AndroidMediaPlayer.x_j.size() > 0) {
            Player player = (Player) AndroidMediaPlayer.x_j.get(0);
            player.close();
            AndroidMediaPlayer.x_j.remove(player);
        }
    }
    
    private void init(String file, String type) {
        x_h = file;
        try {
        	if (file.startsWith("file://"))file = file.replace("file://", "");
	        if (file.startsWith("http://")) {
                AndroidUtils.downloadUrlToFile(file, x_h = new xa(context).x_a(context));
                mediaPlayer.setDataSource(new FileInputStream(new File(x_h)).getFD());
            }else if (file.startsWith("/")) {
				mediaPlayer.setDataSource(new FileInputStream(new File(file)).getFD());
	        }else {mediaPlayer.setDataSource(file);}
		} catch (IOException e) {
			e.printStackTrace();
		}
        if (type == null)type = context.getContentResolver().getType(Uri.parse(file));
        this.type = type;
    }
    
    @Override
    public void addPlayerListener(PlayerListener playerListener) {
        listeners.addElement(playerListener);
    }
    
    @Override
    public void close() {
        if (state == 0) {
            return;
        }
        if (state == 400) {
            deallocate();
        }
        closeImpl();
        state = 0;
        notifyListeners("closed", null);
    }
    
    public void closeImpl() {
        Label_0036: {
            if (mediaPlayer == null) {
                break Label_0036;
            }
            while (true) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    try {
                        mediaPlayer.release();
                        mediaPlayer = null;
                        if (x_i != null) {
                            new File(x_i).delete();
                            x_i = null;
                        }
                        AndroidMediaPlayer.x_j.removeElement(this);
                    }
                    catch (Throwable t) {}
                }
                catch (Throwable t2) {
                    continue;
                }
                break;
            }
        }
    }
    
    @Override
    public void deallocate() { 
    	if (state == 200 || state == 100) {} 
    	else if (state == 0) {
	        throw new IllegalStateException();
	    } else {
	        if (state == 400) {
	            stop();
	        }
	        deallocateImpl();
	        state = 200;
	    }
	}
    
    public void deallocateImpl() {
    }
    
    @Override
    public String getContentType() {
        if (state == 100) {
            throw new IllegalStateException();
        }
        return type;
    }
    
    @Override
    public Control getControl(String s) {
        if (s.endsWith("VolumeControl")) {
            return this;
        }
        return null;
    }
    
    @Override
    public Control[] getControls() {
        return new Control[] { this };
    }
    
    @Override
    public long getDuration() {
        if (state == 0) {
            throw new IllegalStateException();
        }
        long n = -1L;
        if (mediaPlayer != null) {
            n = 1000 * mediaPlayer.getDuration();
        }
        return n;
    }
    
    @Override
    public int getLevel() {
        return x_k;
    }
    
    @Override
    public long getMediaTime() {
        long n = -1L;
        if (mediaPlayer != null) {
            n = 1000 * mediaPlayer.getCurrentPosition();
        }
        return n;
    }
    
    @Override
    public int getState() {
        return state;
    }
    
    public TimeBase getTimeBase() {
        return timeBase;
    }
    
    @Override
    public boolean isMuted() {
        return x_l;
    }
    
    public void notifyListeners(String s, Object o) {
        int r1i = 0;
        while (r1i < listeners.size()) {
            try {
                ((PlayerListener) listeners.elementAt(r1i)).playerUpdate(this, s, o);
            } catch (Exception e) {
            }
            r1i++;
        }
    }
    
    @Override
    public void prefetch() {
        if (state == 100) {
            realize();
        }
        if (state == 300 || state == 400) {
            return;
        }
        if (state == 0) {
            throw new IllegalStateException();
        }
        try {
            prefetchImpl();
            state = 300;
        }
        catch (Exception ex) {
            //throw new MediaException(ex.toString());
        }
    }
    
    public void prefetchImpl() throws IllegalStateException, IOException {
        try {
            mediaPlayer.prepare();
        }
        catch (IOException ex) {
            if (x_g.startsWith("http") && x_h.endsWith(x_g)) {
                x_h = new xa(context).x_a(context);
                AndroidUtils.downloadUrlToFile(x_g, x_h);
                mediaPlayer.reset();
                init(x_h, type);
                mediaPlayer.prepare();
            }
        }
    }
    
    @Override
    public void realize() {
        if (state == 300 || state == 400) {
            return;
        }
        if (state == 0) {
            throw new IllegalStateException();
        }
        try {
            realizeImpl();
            state = 200;
        }
        catch (Exception ex) {
            //throw new MediaException(ex.toString());
        }
    }
    
    public void realizeImpl() {
    }
    
    @Override
    public void removePlayerListener(PlayerListener playerListener) {
        listeners.removeElement(playerListener);
    }
    
    public void setInput(InputStream inputStream, String s) {
        x_g = null;
        init(x_i = new xa(context).getUri(inputStream), s);
    }
    
    public void setInput(String x_g, String s) {
        init(this.x_g = x_g, s);
    }
    
    @Override
    public int setLevel(int x_k) {
        this.x_k = x_k;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(x_k / 100.0f, x_k / 100.0f);
        }
        return x_k;
    }
    
    @Override
    public void setLoopCount(int loopCount) {
        if (state == 400 || state == 0) {
            throw new IllegalStateException();
        }
        this.loopCount = loopCount;
    }
    
    @Override
    public long setMediaTime(long desiredTime) {
    	this.desiredTime = desiredTime;
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int)(desiredTime / 1000L));
        }
        return desiredTime;
    }
    
    @Override
    public void setMute(boolean x_l) {
        if (x_l) {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.0f, 0.0f);
            }
        }
        else if (mediaPlayer != null) {
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
        this.x_l = x_l;
    }
    
    public void setSourceUrl(String s) {
    }
    
    public void setTimeBase(TimeBase x_c) {
        if (state == 100 || state == 400) {
            throw new IllegalStateException();
        }
        timeBase = x_c;
    }
    
    @Override
    public void start(){
        if (state == 0) {
            throw new IllegalStateException();
        }
        if (state == 400) {
            return;
        }
        if (state == 100 || state == 200) {
            prefetch();
        }
        try {
            startImpl();
            state = 400;
            notifyListeners("started", new Long(1000 * mediaPlayer.getCurrentPosition()));
        }
        catch (Exception ex2) {
            //throw new MediaException(ex2.toString());
        }
    }
    
    public void startImpl() {
        mediaPlayer.start();
    }
    
    @Override
    public void stop() {
        if (state == 0) {
            throw new IllegalStateException();
        }
        if (state != 400) {
            return;
        }
        state = 300;
        while (true) {
            try {
                stopImpl();
                notifyListeners("stopped", new Long(1000 * mediaPlayer.getCurrentPosition()));
            }
            catch (Exception ex) {
                continue;
            }
            break;
        }
    }
    
    public void stopImpl() throws Exception {
        try {
            mediaPlayer.pause();
        }
        catch (Exception ex) {
            throw ex;
        }
    }
}
