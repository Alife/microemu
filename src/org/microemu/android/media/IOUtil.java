package org.microemu.android.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
	public static final int copyStream(InputStream is,OutputStream os, int paramInt) {
		return copyStream(is, os,new byte[paramInt]);
	}

	public static final int copyStream(InputStream is,OutputStream paramOutputStream, byte[] by) {
		int i = by.length;
		int j = 0;
		while (true) {
			int k;
			try {
				k = is.read(by, 0, i);
				if (k < 0)break;
				paramOutputStream.write(by, 0, k);
				j += k;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return j;
	}
}
