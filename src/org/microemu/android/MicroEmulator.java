/**
 *  MicroEmulator
 *  Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
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
 *  @version $Id: MicroEmulator.java 2517 2011-11-10 12:30:37Z barteo@gmail.com $
 */

package org.microemu.android;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.MIDletEntry;
import org.microemu.android.device.AndroidDevice;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.android.device.ui.AndroidCommandUI;
import org.microemu.android.device.ui.AndroidDisplayableUI;
import org.microemu.android.util.AndroidLoggerAppender;
import org.microemu.android.util.AndroidRecordStoreManager;
import org.microemu.android.util.AndroidRepaintListener;
import org.microemu.app.Common;
import org.microemu.app.launcher.Launcher;
import org.microemu.app.util.MIDletSystemProperties;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.device.ui.CommandUI;
import org.microemu.log.Logger;
import org.microemu.util.JadMidletEntry;
import org.microemu.util.JadProperties;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;

public class MicroEmulator extends MicroEmulatorActivity {
	
	public static final String LOG_TAG = "MicroEmulator";
		
	public Common common;
	
	public MIDlet midlet;
	
	public MIDletAccess ma;
	public DisplayAccess da;
	public AndroidDisplayableUI ui;

	/** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
		
        Logger.removeAllAppenders();
        Logger.setLocationEnabled(false);
        Logger.addAppender(new AndroidLoggerAppender());
        
        System.setOut(new PrintStream(new OutputStream() {
        	StringBuffer line = new StringBuffer();
			@Override
			public void write(int oneByte) throws IOException {
				if (((char) oneByte) == '\n') {
					Logger.debug(line.toString());
					if (line.length() > 0) {
						line.delete(0, line.length() - 1);
					}
				} else {line.append((char) oneByte);}
			}
        }));
        
        System.setErr(new PrintStream(new OutputStream() {
        	StringBuffer line = new StringBuffer();
			@Override
			public void write(int oneByte) throws IOException {
				if (((char) oneByte) == '\n') {
					Logger.debug(line.toString());
					if (line.length() > 0) {
						line.delete(0, line.length() - 1);
					}
				} else {line.append((char) oneByte);}
			}
        }));
        
        java.util.List<String> params = new ArrayList<String>();
        params.add("--usesystemclassloader");
        params.add("--quit");
        
        common = new Common(emulatorContext);
        String midletClassName=Launcher.class.getName();
        Vector<JadMidletEntry> midlets = null;
        try {
	        common.jad = new JadProperties();
	        common.jad.read(getAssets().open(AndroidConfig.MANIFEST));
	        midlets=common.jad.getMidletEntries();
	        if (midlets!=null&&midlets.size()==1){
	        	midletClassName=(midlets.get(0)).getClassName();
	        	params.add(midletClassName);
	        }	       
		} catch (Exception e) {
			Logger.error(e);
		}
		
        common.setRecordStoreManager(new AndroidRecordStoreManager(this));
        common.setDevice(new AndroidDevice(emulatorContext, this));        
        common.initParams(params, null, AndroidDevice.class);
               
        System.setProperty("microedition.platform", "microemu-android");
        System.setProperty("microedition.configuration", "CLDC-1.1");
        System.setProperty("microedition.profiles", "MIDP-2.0");
        System.setProperty("microedition.locale", Locale.getDefault().toString());

        /* JSR-75 */
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("fsRoot", "/");
        // comment for show all file system on directory is '/'
        //properties.put("fsSingle", "/");
        common.registerImplementation("org.microemu.cldc.file.FileSystem", properties, false);
        MIDletSystemProperties.setPermission("javax.microedition.io.Connector.file.read", 1);
        MIDletSystemProperties.setPermission("javax.microedition.io.Connector.file.write", 1);
        System.setProperty("fileconn.dir.photos", "file://sdcard/");

        initializeExtensions();
        
        common.setSuiteName(midletClassName);
        
        if(midlets!=null&&midlets.size()>1) 
	        for (JadMidletEntry jadEntry : midlets) { 
	        	Class<?> midletClass;
				try {midletClass = Class.forName(jadEntry.getClassName());} 
				catch (ClassNotFoundException e) {continue;}
				MIDletEntry entry=new MIDletEntry(jadEntry.getName(), midletClass);
				Launcher.addMIDletEntry(entry);
	    }       
        //midlet = common.initMIDlet(false);

    }
	@Override
    protected void onPause() {
        super.onPause();
        
        if (contentView != null) {
            if (contentView instanceof AndroidRepaintListener) {
                ((AndroidRepaintListener) contentView).onPause();
            }
        }

        if(midlet!=null)ma = MIDletBridge.getMIDletAccess(midlet);
        if (ma != null) {
            ma.pauseApp();
            ma.getDisplayAccess().hideNotify();
        }
    }

	@Override
    protected void onResume() {
        super.onResume();
        setConfig(getPreferences(config,getSharedPreferences(AndroidConfig.Name, 0)));
	    
        new Thread(new Runnable() {

            public void run()
            {
                if(midlet!=null)ma = MIDletBridge.getMIDletAccess(midlet);
                if (ma != null) {
                    try {
                        ma.startApp();
                    } catch (MIDletStateChangeException e) {
                        e.printStackTrace();
                    }
                }

                if (contentView != null) {
                    if (contentView instanceof AndroidRepaintListener) {
                        ((AndroidRepaintListener) contentView).onResume();
                    }
                    post(new Runnable() {
                        public void run() {
                            contentView.invalidate();
                        }
                    });
                }

		        if(!isFirstResume&&!windowFullscreen)
		        	post(new Runnable() {
			            public void run() {
			            	LayoutParams params = new WindowManager.LayoutParams();
			            	params.x = !windowFullscreen?statusBarHeight:0;
						    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
			            }
			        });
		    	isFirstResume = false;
	    	}
        }).start();
    }
    
	protected void initializeExtensions() {
    }

    //private boolean ignoreBackKeyUp = false;

	int menuClickTime = 0;
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode=event.getKeyCode(),keyAction=event.getAction(),unicodeChar=event.getUnicodeChar();
    	
        if (ignoreKey(keyCode)) {return super.dispatchKeyEvent(event);}	
        MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {return false;}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {return false;}
		if (da.getCurrent() == null) {return false;}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {return false;}
		if(keyAction==KeyEvent.ACTION_DOWN){

//			if(keyCode==KeyEvent.KEYCODE_MENU){
//			menuClickTime++;
//			if(menuClickTime==3){
//				Intent intent = new Intent();
//				intent.setClass(MicroEmulator.this,SettingsActivity.class);
//				startActivity(intent);menuClickTime=0;
//			}else if(menuClickTime==2){
//				new Thread("menuClickTime"){
//					@Override
//					public void run() {	
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//						post(new Runnable() {
//				            public void run() {
//				            	menuClickTime=0;
//				    			Log.i(LOG_TAG, "menuClickTime:"+menuClickTime);
//				            }
//				        });
//					}
//				}.start();
//			}}
//			
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				List<AndroidCommandUI> commands = ui.getCommandsUI();
				
				CommandUI cmd = getFirstCommandOfType(commands, Command.BACK);
				if (cmd != null) {
					if (ui.getCommandListener() != null) {
					//ignoreBackKeyUp = true;
						MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
					}
					return true;
				}
	
				cmd = getFirstCommandOfType(commands, Command.EXIT);
				if (cmd != null) {
					if (ui.getCommandListener() != null) {
					//ignoreBackKeyUp = true;
						MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
					}
					return true;
				}
				
				cmd = getFirstCommandOfType(commands, Command.CANCEL);
				if (cmd != null) {
					if (ui.getCommandListener() != null) {
					//ignoreBackKeyUp = true;
						MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
					}
					return true;
				}
			}
			if (keyCode == KeyEvent.KEYCODE_MENU){
			//windowFullscreen=false;
			//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				return false;
			}
		}
				
//		Log.i(LOG_TAG, "keyAction:"+keyAction+
//				" keyCode:"+keyCode+" menuClickTime:"+menuClickTime+
//				" Char:"+event.getCharacters());

		int pressTime = 0;
		if(AndroidConfig.Setting_SupportNumKey){
		// support Num KeyEvent for opera mini mod
		if(keyCode==KeyEvent.KEYCODE_0){pressTime=10;}
		else if(keyCode==KeyEvent.KEYCODE_1)pressTime=21;
		else if(keyCode>=KeyEvent.KEYCODE_2&&keyCode<=KeyEvent.KEYCODE_6)pressTime=3;
		else if(keyCode==KeyEvent.KEYCODE_8)pressTime=3;
		else if(keyCode==KeyEvent.KEYCODE_7||keyCode==KeyEvent.KEYCODE_9)pressTime=4;
		}
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER :
			keyCode = -5;
			break;
		case KeyEvent.KEYCODE_DPAD_UP :
			keyCode = -1;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN :
			keyCode = -2;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT :
			keyCode = -3;
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT :
			keyCode = -4;
			break;
		case KeyEvent.KEYCODE_DEL :
			keyCode = 127;
			break;
		default: 
            String ch=event.getCharacters();
//		Log.i(LOG_TAG, "keyAction:"+keyAction+
//				" keyCode:"+keyCode+" menuClickTime:"+menuClickTime+
//				" Char:"+event.getCharacters());
			if(ch!=null){if(ch.length()>0)keyCode=(int)ch.charAt(0);}else{if(unicodeChar!=0)keyCode=unicodeChar;else keyCode=-127-keyCode;}
//		Log.i(LOG_TAG, "keyAction:"+keyAction+
//				" keyCode:"+keyCode+" menuClickTime:"+menuClickTime+
//				" Char:"+event.getCharacters());
		}

		if (ui instanceof AndroidCanvasUI) {
			Device device = DeviceFactory.getDevice();
			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
			if(keyAction==KeyEvent.ACTION_DOWN)inputMethod.buttonPressed(keyCode);
			else if(keyAction==KeyEvent.ACTION_UP)inputMethod.buttonReleased(keyCode);
			else { // keyAction==KeyEvent.ACTION_MULTIPLE
//				inputMethod.buttonPressed(keyCode).buttonReleased(keyCode);
//				// support Chinese character
//				if(event.getCharacters()!=null&&event.getCharacters().length()>1){
//					for (int i = 1; i < event.getCharacters().length(); i++) {
//						int secondKeyCode = event.getCharacters().charAt(i);
//						inputMethod.buttonPressed(secondKeyCode).buttonReleased(secondKeyCode);
//					}
//				}
			}
//			if(keyAction==KeyEvent.ACTION_UP&&pressTime>0){
//				for (int ii = 0; ii < pressTime; ii++) {
//					inputMethod.buttonPressed(keyCode).buttonReleased(keyCode);
//				}
//				// support input number twice 
//				dispatchKeyEvent(new KeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT));
//			}
			return true;
		}

	    return super.dispatchKeyEvent(event);
	}

    /*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return false;
		}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return false;
		}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {
			return false;
		}		

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			List<AndroidCommandUI> commands = ui.getCommandsUI();
			
			CommandUI cmd = getFirstCommandOfType(commands, Command.BACK);
			if (cmd != null) {
				if (ui.getCommandListener() != null) {
					ignoreBackKeyUp = true;
					MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
				}
				return true;
			}

			cmd = getFirstCommandOfType(commands, Command.EXIT);
			if (cmd != null) {
				if (ui.getCommandListener() != null) {
					ignoreBackKeyUp = true;
					MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
				}
				return true;
			}
			
			cmd = getFirstCommandOfType(commands, Command.CANCEL);
			if (cmd != null) {
				if (ui.getCommandListener() != null) {
					ignoreBackKeyUp = true;
					MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(cmd.getCommand(), da.getCurrent());
				}
				return true;
			}
		}
					
		if (ui instanceof AndroidCanvasUI) {
            if (ignoreKey(keyCode)) {
                return false;    
            }                 

			Device device = DeviceFactory.getDevice();
			((AndroidInputMethod) device.getInputMethod()).buttonPressed(event);
			Log.d("MicroemuInput","Pressed="+String.valueOf(keyCode));

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && ignoreBackKeyUp) {
			ignoreBackKeyUp = false;
			return true;
		}
		
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return false;
		}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return false;
		}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {
			return false;
		}		

		if (ui instanceof AndroidCanvasUI) {
			if (ignoreKey(keyCode)) {
	            return false;    
	        }
	
			Device device = DeviceFactory.getDevice();
			((AndroidInputMethod) device.getInputMethod()).buttonReleased(event);
			Log.d("MicroemuInput","Released="+String.valueOf(keyCode));
			return true;
		}

		return super.onKeyUp(keyCode, event);
		
	}*/
	
	private CommandUI getFirstCommandOfType(List<AndroidCommandUI> commands, int commandType) {
		for (int i = 0; i < commands.size(); i++) {
			CommandUI cmd = commands.get(i);
			if (cmd.getCommand().getCommandType() == commandType) {
				return cmd;
			}
		}	
		
		return null;
	}
	
    private boolean ignoreKey(int keyCode) {
        switch (keyCode) {
        //case KeyEvent.KEYCODE_VOLUME_DOWN:
        //case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
		case KeyEvent.KEYCODE_ALT_LEFT:  
            return true;
        default:
            return false;
        }    
    }
	
    public final static KeyEvent KEY_RIGHT_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT); 
    public final static KeyEvent KEY_RIGHT_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT);
    public final static KeyEvent KEY_LEFT_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT);
    public final static KeyEvent KEY_LEFT_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT);
    public final static KeyEvent KEY_DOWN_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
    public final static KeyEvent KEY_DOWN_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN);
    public final static KeyEvent KEY_UP_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
    public final static KeyEvent KEY_UP_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP);
    public final static float TRACKBALL_THRESHOLD = 0.4f; 
    public float accumulatedTrackballX = 0;
	public float accumulatedTrackballY = 0;
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			MIDletAccess ma = MIDletBridge.getMIDletAccess();
			if (ma == null) {
				return false;
			}
			final DisplayAccess da = ma.getDisplayAccess();
			if (da == null) {
				return false;
			}
			AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
			if (ui instanceof AndroidCanvasUI) {
				float x = event.getX();
				float y = event.getY();
				if ((x > 0 && accumulatedTrackballX < 0) || (x < 0 && accumulatedTrackballX > 0)) {
					accumulatedTrackballX = 0;
				}
				if ((y > 0 && accumulatedTrackballY < 0) || (y < 0 && accumulatedTrackballY > 0)) {
					accumulatedTrackballY = 0;
				}
				if (accumulatedTrackballX + x > TRACKBALL_THRESHOLD) {
					accumulatedTrackballX -= TRACKBALL_THRESHOLD;
					KEY_RIGHT_DOWN_EVENT.dispatch(this);
					KEY_RIGHT_UP_EVENT.dispatch(this);
				} else if (accumulatedTrackballX + x < -TRACKBALL_THRESHOLD) {
					accumulatedTrackballX += TRACKBALL_THRESHOLD;
					KEY_LEFT_DOWN_EVENT.dispatch(this);
					KEY_LEFT_UP_EVENT.dispatch(this);
				}
				if (accumulatedTrackballY + y > TRACKBALL_THRESHOLD) {
					accumulatedTrackballY -= TRACKBALL_THRESHOLD;
					KEY_DOWN_DOWN_EVENT.dispatch(this);
					KEY_DOWN_UP_EVENT.dispatch(this);
				} else if (accumulatedTrackballY + y < -TRACKBALL_THRESHOLD) {
					accumulatedTrackballY += TRACKBALL_THRESHOLD;
					KEY_UP_DOWN_EVENT.dispatch(this);
					KEY_UP_UP_EVENT.dispatch(this);
				}
				accumulatedTrackballX += x;
				accumulatedTrackballY += y;
				
				return true;
			}
		}
		
		return super.onTrackballEvent(event);
	}	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();	
	
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return false;
		}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return false;
		}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {
			return false;
		}		
		
		boolean result = false;
		List<AndroidCommandUI> commands = ui.getCommandsUI();
		for (int i = 0; i < commands.size(); i++) {
			result = true;
			AndroidCommandUI cmd = commands.get(i);
			if (cmd.getCommand().getCommandType() != Command.BACK && cmd.getCommand().getCommandType() != Command.EXIT) {
				SubMenu item = menu.addSubMenu(Menu.NONE, i + Menu.FIRST, Menu.NONE, cmd.getCommand().getLabel());
				item.setIcon(cmd.getDrawable());
			}
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null)return false;
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null)return false;
		if (da.getCurrent() == null) {return false;}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {return false;}

		int commandIndex = item.getItemId() - Menu.FIRST;
		List<AndroidCommandUI> commands = ui.getCommandsUI();
		if(commandIndex<0||commandIndex>commands.size())return false;
		CommandUI c = commands.get(commandIndex);

		if (c != null) {
			MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(c.getCommand(), da.getCurrent());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}

