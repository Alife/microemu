package org.microemu.android.media;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AndroidUtils {
	public static void downloadUrlToFile(String p1,String p2) throws FileNotFoundException {
		downloadUrl(p1, new FileOutputStream(p2));
	}

	private static void downloadUrl(String p1, FileOutputStream fileOutputStream) {
		// TODO Auto-generated method stub
		
	}
}
