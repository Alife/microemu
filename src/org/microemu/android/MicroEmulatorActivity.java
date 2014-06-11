/**
 *  MicroEmulator
 *  Copyright (C) 2009 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 *
 *  @version $Id: MicroEmulatorActivity.java 1918 2009-01-21 12:56:43Z barteo $
 */

package org.microemu.android;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.microedition.io.ConnectionNotFoundException;

import org.android.annotation.DisableView;
import org.android.annotation.Entries;
import org.microemu.DisplayComponent;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.android.device.AndroidFontManager;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.util.ActivityResultListener;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.EmulatorContext;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.log.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

public abstract class MicroEmulatorActivity extends Activity {
		
	public static AndroidConfig config = new AndroidConfig();
	
	private Handler handler = new Handler();
	
	private Thread activityThread;
	
	protected View contentView;

	private Dialog dialog;
	
	private ArrayList<ActivityResultListener> activityResultListeners = new ArrayList<ActivityResultListener>();
	
	protected EmulatorContext emulatorContext;

	public boolean windowFullscreen = true;
	protected int statusBarHeight = 0;
	/**
	 * execute switch screen onResume except first time
	 */
	protected boolean isFirstResume = true;
	int width = 0;
	int height = 0;

	protected boolean added = false;

	protected Display display;
	public void setConfig(AndroidConfig config) {
		MicroEmulatorActivity.config = config;
	}

	public EmulatorContext getEmulatorContext() {
		return emulatorContext;
	}

	public boolean post(Runnable r) {
		if (activityThread == Thread.currentThread()) {
			r.run();
			return true;
		} else {
			return handler.post(r);
		}
	}
	
	public boolean postDelayed(Runnable r, long delayMillis) {
		if (activityThread == Thread.currentThread()) {
			r.run();
			return true;
		} else {
			return handler.postDelayed(r, delayMillis);
		}
	}
	
	public boolean isActivityThread() {
		return (activityThread == Thread.currentThread());
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Query the activity property android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
		//TypedArray ta = getTheme().obtainStyledAttributes(new int[] { android.R.attr.windowFullscreen });
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//windowFullscreen = ta.getBoolean(0, false);
			//setTitle("aaa");
		windowFullscreen=AndroidConfig.Screen_DefaultFull;
		Drawable phoneCallIcon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
		//android.util.Log.i(MicroEmulator.LOG_TAG, "config: Fullscreen "+config.Screen_DefaultFull);
		statusBarHeight  = phoneCallIcon.getIntrinsicHeight();

    	if(android.os.Build.VERSION.SDK_INT <= 4)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
						   WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		
		display = getWindowManager().getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();

		emulatorContext = new EmulatorContext() {

			private InputMethod inputMethod = new AndroidInputMethod();

			private DeviceDisplay deviceDisplay = new AndroidDeviceDisplay(MicroEmulatorActivity.this, this, width, height);

			private FontManager fontManager = new AndroidFontManager(getResources().getDisplayMetrics());

			public DisplayComponent getDisplayComponent() {
				// TODO consider removal of EmulatorContext.getDisplayComponent()
				System.out.println("MicroEmulator.emulatorContext::getDisplayComponent()");
				return null;
			}

			public InputMethod getDeviceInputMethod() {
				return inputMethod;
			}

			public DeviceDisplay getDeviceDisplay() {
				return deviceDisplay;
			}

			public FontManager getDeviceFontManager() {
				return fontManager;
			}

			public InputStream getResourceAsStream(Class origClass, String name) {
				try {
					if (name.startsWith("/")) {
						return MicroEmulatorActivity.this.getAssets().open(name.substring(1));
					} else {
						Package p = origClass.getPackage();
						if (p == null) {
							return MicroEmulatorActivity.this.getAssets().open(name);
						} else {
							String folder = origClass.getPackage().getName().replace('.', '/');
							return MicroEmulatorActivity.this.getAssets().open(folder + "/" + name);
						}
					}
				} catch (IOException e) {
					Logger.debug(e);
					return null;
				}
			}

			public boolean platformRequest(String url) throws ConnectionNotFoundException
			{
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.parse(url);
					if(uri.getScheme().startsWith("file")){
						String type ="";
						String extension = MimeTypeMap.getFileExtensionFromUrl(url);
						if(extension==null||extension.equals(""))/* 取得扩展名 */  
							extension=url.substring(url.lastIndexOf(".")+1,url.length()).toLowerCase(); 
						type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
						if(type==null&&MIME_MapTable.containsKey(extension)){type = MIME_MapTable.get(extension);}
						if(type==null){type = "text/plain";}
						if(extension.equalsIgnoreCase("apk"))intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						//setType 调用后设置 mimeType，然后将 data 置为 null；
						//setData 调用后设置 data，然后将 mimeType 置为 null；
						//setDataAndType 调用后才会同时设置 data 与 mimeType。
						intent.setDataAndType(uri, type);
					}else intent.setData(uri);
					Log.i(MicroEmulator.LOG_TAG, "platformRequest "+intent.getDataString() +" type:"+intent.getType());
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					throw new ConnectionNotFoundException(e.getMessage());
				}
				return true;
			}
		   };
		
		activityThread = Thread.currentThread();
	}
	

	public View getContentView() {
		return contentView;
	}

	@Override
	public void setContentView(View view) {
		Log.d("AndroidCanvasUI", "set content view: " + view);                			
		super.setContentView(view);
		
		contentView = view;
	}
		
	public void addActivityResultListener(ActivityResultListener listener) {
		activityResultListeners.add(listener);
	}
	
	public void removeActivityResultListener(ActivityResultListener listener) {
		activityResultListeners.remove(listener);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		for (Iterator<ActivityResultListener> it = activityResultListeners.iterator(); it.hasNext(); ) {
			if (it.next().onActivityResult(requestCode, resultCode, data)) {
				return;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
		if (dialog != null) {
			showDialog(0);
		} else {
			removeDialog(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return dialog;
	}

	
	public <T> T getPreferences(T t,SharedPreferences preferences) {
		Field[] fields = t.getClass().getFields();
		for (Field field : fields) {
			DisableView disEnabled = field.getAnnotation(DisableView.class);
			if(disEnabled!=null)continue;
			Class<?> type = field.getType(); 
			String fieldName=field.getName(); 
			try {
				Entries entries = field.getAnnotation(Entries.class);
				if(entries==null){
					
					if(type.getPackage()!=null&&!type.getPackage().getName().startsWith("java.lang"))
						getPreferences(field.get(t),preferences);
					if(type.equals(String.class)){
						String value = preferences.getString(fieldName, field.get(t).toString());
						field.set(t, value);
					}else if(type.equals(int.class)||type.equals(byte.class)){
						int value = preferences.getInt(fieldName, field.getInt(t));
						preferences.getInt(fieldName, Integer.parseInt(field.getInt(t)+""));
						field.set(t, value);
					}else if(type.equals(Integer.class)||type.equals(Byte.class)){
						Integer value = preferences.getInt(fieldName, field.getInt(t));
						field.set(t, value);
					}else if(type.equals(boolean.class)){
						boolean value = preferences.getBoolean(fieldName, field.getBoolean(t));
						field.set(t, value);
					}else if(type.equals(Boolean.class)){
						Boolean value = preferences.getBoolean(fieldName,field.getBoolean(t));
						field.set(t, value);
					}else if(type.equals(Date.class)){
						float value = preferences.getFloat(fieldName, ((Date)field.get(t)).getTime());
						field.set(t, value);
					}else if(type.equals(Float.class)||type.equals(float.class)){
						Float value = preferences.getFloat(fieldName, field.getFloat(t));
						field.set(t, value);
					}else if(type.equals(Double.class)||type.equals(double.class)){
						Float value = preferences.getFloat(fieldName, field.getFloat(t));
						field.set(t, value);
					}else continue;
				}
				else {
					// ListPreference 保存的只能是 String
					String value = preferences.getString(fieldName, field.get(t).toString());
					field.set(t, Integer.parseInt(value));
				}
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return t;
	}

	//建立一个MIME类型与文件后缀名的匹配表
	private static final HashMap<String, String> MIME_MapTable = new HashMap<String, String>(){{
		//{后缀名，    MIME类型}
		put("3gp","video/3gpp");
		put("apk","application/vnd.android.package-archive");
		put("asf","video/x-ms-asf");
		put("avi","video/x-msvideo");
		put("bin","application/octet-stream");
		put("bmp","image/bmp");
		put("c","text/plain");
		put("class","application/octet-stream");
		put("conf","text/plain");
		put("cpp","text/plain");
		put("doc","application/msword");
		put("exe","application/octet-stream");
		put("gif","image/gif");
		put("gtar","application/x-gtar");
		put("gz","application/x-gzip");
		put("h","text/plain");
		put("htm","text/html");
		put("html","text/html");
		put("jad","text/vnd.sun.j2me.app-descriptor");
		put("jar","application/java-archive");
		put("java","text/plain");
		put("jpeg","image/jpeg");
		put("jpg","image/jpeg");
		put("js","application/x-javascript");
		put("log","text/plain");
		put("m3u","audio/x-mpegurl");
		put("m4a","audio/mp4a-latm");
		put("m4b","audio/mp4a-latm");
		put("m4p","audio/mp4a-latm");
		put("m4u","video/vnd.mpegurl");
		put("m4v","video/x-m4v");
		put("mov","video/quicktime");
		put("mp2","audio/x-mpeg");
		put("mp3","audio/x-mpeg");
		put("mp4","video/mp4");
		put("mpc","application/vnd.mpohun.certificate");
		put("mpe","video/mpeg");
		put("mpeg","video/mpeg");
		put("mpg","video/mpeg");
		put("mpg4","video/mp4");
		put("mpga","audio/mpeg");
		put("msg","application/vnd.ms-outlook");
		put("ogg","audio/ogg");
		put("pdf","application/pdf");
		put("png","image/png");
		put("pps","application/vnd.ms-powerpoint");
		put("ppt","application/vnd.ms-powerpoint");
		put("prop","text/plain");
		put("rar","application/x-rar-compressed");
		put("rc","text/plain");
		put("rmvb","audio/x-pn-realaudio");
		put("rtf","application/rtf");
		put("sh","text/plain");
		put("tar","application/x-tar");
		put("tgz","application/x-compressed");
		put("txt","text/plain");
		put("wav","audio/x-wav");
		put("wma","audio/x-ms-wma");
		put("wmv","audio/x-ms-wmv");
		put("wps","application/vnd.ms-works");
		put("xml","text/xml");
		put("xml","text/plain");
		put("z","application/x-compress");
		put("zip","application/zip");
	}};
}
