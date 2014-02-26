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
import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.io.ConnectionNotFoundException;

import org.microemu.DisplayAccess;
import org.microemu.DisplayComponent;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.android.device.AndroidFontManager;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.util.ActivityResultListener;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.DeviceFactory;
import org.microemu.device.EmulatorContext;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.log.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.view.WindowManager.LayoutParams;



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
	 * thread sleep time on switch screen 
	 */
	protected int sleepTimeOnSwitchScreen = 5000;
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
		//windowFullscreen = ta.getBoolean(0, false);
		    //requestWindowFeature(Window.FEATURE_NO_TITLE);
			//setTitle("aaa");
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
//                           WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		windowFullscreen=true;
		Drawable phoneCallIcon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
		if (!windowFullscreen) {
			statusBarHeight  = phoneCallIcon.getIntrinsicHeight();
		}
		
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (ActivityNotFoundException e) {
                    throw new ConnectionNotFoundException();
                }

                return true;
            }
                    
        };
		
		activityThread = Thread.currentThread();
	
		int currentapiVersion=android.os.Build.VERSION.SDK_INT;
		// just for first start, this time must greater than the OPM jar loading time on the screen
		// or switch screen must under the OPM jar loading on the screen
		if(currentapiVersion<15)sleepTimeOnSwitchScreen = 1000;
		else sleepTimeOnSwitchScreen = 5000;
	}
	
	public View getContentView() {
		return contentView;
	}

	@Override
	public void setContentView(View view) {
		Log.d("AndroidCanvasUI", "set content view: " + view);                			
		super.setContentView(view);
		
		contentView = view;
		
		if(!windowFullscreen)switchFullscreen();
	}
		
	/**
	 * switch full screen and display the status bar
	 */
	void switchFullscreen() {
		new Thread("WindowManager"){
			@Override
			public void run() {				
		        try {
					Thread.sleep(sleepTimeOnSwitchScreen);
			        postDelayed(new Runnable() {
			            public void run() {
			                LayoutParams params = new WindowManager.LayoutParams();
			                params.x = statusBarHeight;
						    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
			        		Log.d("AndroidCanvasUI", "onDoubleTap: " + statusBarHeight);  
			            }
			        },1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Drawable phoneCallIcon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
		int statusBarHeight = 0;
		if (!windowFullscreen) {
			statusBarHeight = phoneCallIcon.getIntrinsicHeight();
			//if(newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES)getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                          //  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
        Display display = getWindowManager().getDefaultDisplay();
		AndroidDeviceDisplay deviceDisplay = (AndroidDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
		deviceDisplay.setSize(display.getWidth(), display.getHeight());
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return;
		}
		DisplayAccess da = ma.getDisplayAccess();
		if (da != null) {
			da.sizeChanged();
			deviceDisplay.repaint(0, 0, deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight());
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
	
}
