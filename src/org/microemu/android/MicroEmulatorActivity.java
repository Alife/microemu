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
import java.util.Iterator;

import javax.microedition.io.ConnectionNotFoundException;

import org.microemu.DisplayComponent;
import org.microemu.android.annotation.DisableView;
import org.microemu.android.annotation.Entries;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
		setConfig(getPreferences(config,PreferenceManager.getDefaultSharedPreferences(this)));
	    // Query the activity property android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
		//TypedArray ta = getTheme().obtainStyledAttributes(new int[] { android.R.attr.windowFullscreen });
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//windowFullscreen = ta.getBoolean(0, false);
			//setTitle("aaa");
		windowFullscreen=config.Screen_DefaultFull;
		Drawable phoneCallIcon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
    	android.util.Log.i(MicroEmulator.LOG_TAG, "config: Fullscreen "+config.Screen_DefaultFull);
		statusBarHeight  = phoneCallIcon.getIntrinsicHeight();

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
                	Uri uri = Uri.parse(url);
                	String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                    String type ="*/*";
					if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
					if(type==null){/* 取得扩展名 */  
						extension=url.substring(url.lastIndexOf(".")+1,url.length()).toLowerCase(); 
					    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
					}
					if(type==null) type="text/plain";
                	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.setType(type);
                	Log.i(MicroEmulator.LOG_TAG, "platformRequest "+url +" type:"+type);
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
		
		if(!windowFullscreen){
			new Thread("WindowManager"){
				@Override
				public void run() {	
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        post(new Runnable() {
			            public void run() {
			            	LayoutParams params = new WindowManager.LayoutParams();
			            	params.x = !windowFullscreen?statusBarHeight:0;
						    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
			            }
			        });
				}
			}.start();
		}
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

}
