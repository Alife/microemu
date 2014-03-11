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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.AndroidDevice;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.android.device.ui.AndroidCommandUI;
import org.microemu.android.device.ui.AndroidDisplayableUI;
import org.microemu.android.util.AndroidLoggerAppender;
import org.microemu.android.util.AndroidRecordStoreManager;
import org.microemu.android.util.AndroidRepaintListener;
import org.microemu.app.Common;
import org.microemu.app.util.MIDletSystemProperties;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.device.ui.CommandUI;
import org.microemu.log.Logger;
import org.microemu.util.JadProperties;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
public class MicroEmulator extends MicroEmulatorActivity implements OnTouchListener {
	
	public static final String LOG_TAG = "MicroEmulator";
		
	public Common common;
	
	private MIDlet midlet;
	
	MIDletAccess ma;
	DisplayAccess da;
	AndroidDisplayableUI ui;

	/** Called when the activity is first created. */
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
				} else {
					line.append((char) oneByte);
				}
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
				} else {
					line.append((char) oneByte);
				}
			}
        	
        }));
        
        java.util.List<String> params = new ArrayList<String>();
        params.add("--usesystemclassloader");
        params.add("--quit");
        
        String midletClassName;
        String jadName = null;
		try {
			Class<?> r = Class.forName(getComponentName().getPackageName() + ".R$string");
	        midletClassName = getResources().getString(r.getField("class_name").getInt(null));
            try {
                jadName = getResources().getString(r.getField("jad_name").getInt(null));
            } catch (NoSuchFieldException e) {
            }

	        params.add(midletClassName);	       
		} catch (Exception e) {
			Logger.error(e);
			return;
		}

        common = new Common(emulatorContext);
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
        //properties.put("fsSingle", "/");
        common.registerImplementation("org.microemu.cldc.file.FileSystem", properties, false);
        MIDletSystemProperties.setPermission("javax.microedition.io.Connector.file.read", 1);
        MIDletSystemProperties.setPermission("javax.microedition.io.Connector.file.write", 1);
        System.setProperty("fileconn.dir.photos", "file://sdcard/");

        if (jadName != null) {
            try {
    	        InputStream is = getAssets().open(jadName);
    	        common.jad = new JadProperties();
    	        common.jad.read(is);
            } catch (Exception e) {
            	Logger.error(e);
            }
        }
        
        initializeExtensions();
        
        common.setSuiteName(midletClassName);
        midlet = common.initMIDlet(false);

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
	public void setContentView(View view) {
		super.setContentView(view);
        view.setOnTouchListener(this);
	}

	@Override
    protected void onResume() {
        super.onResume();
        setConfig(getPreferences(config,PreferenceManager.getDefaultSharedPreferences(this)));
	    
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

    private boolean ignoreBackKeyUp = false;

    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode=event.getKeyCode(),keyAction=event.getAction(),unicodeChar=event.getUnicodeChar();
        if (ignoreKey(keyCode)) {return super.dispatchKeyEvent(event);}	
        MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {return false;}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {return false;}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {return false;}
		if(keyAction==KeyEvent.ACTION_DOWN){
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
			if (keyCode == KeyEvent.KEYCODE_MENU){
			//windowFullscreen=false;
			//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				return false;
			}
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
			if(ch!=null){if(ch.length()>0)keyCode=(int)ch.charAt(0);} else{if(unicodeChar!=0)keyCode=unicodeChar;else keyCode=-127-keyCode;}
		}
	    //if(event.getKeyCode()!=0)Log.d("key pressed", String.valueOf(event.getKeyCode()));
	    //else Log.d("key pressed_", String.valueOf((int)(event.getCharacters().toString().charAt(0))));
	
		if (ui instanceof AndroidCanvasUI) {
			Device device = DeviceFactory.getDevice();
			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
			if(!_keyCodeTest){
				if(keyAction==KeyEvent.ACTION_DOWN)inputMethod.buttonPressed(keyCode);
				else if(keyAction==KeyEvent.ACTION_UP)inputMethod.buttonReleased(keyCode);
				else {
					Log.i(LOG_TAG, "keyAction: "+keyAction+"keyCode: "+keyCode+"Char: "+event.getCharacters());
					inputMethod.buttonPressed(keyCode).buttonReleased(keyCode);
					// support Chinese word
					if(event.getCharacters().length()>1){
						for (int i = 1; i < event.getCharacters().length(); i++) {
							int j = event.getCharacters().charAt(i);
							inputMethod.buttonPressed(j).buttonReleased(j);
						}
					}
				}
			}
			else {
				if(keyCode == -151)_keyCode = _keyCode +1;
				if(keyCode == -152)_keyCode = _keyCode -1;
				if(keyCode == -211)_keyCode = 0;
				inputMethod.buttonPressed(_keyCode).buttonReleased(_keyCode);
				Log.i(LOG_TAG, " _keyCode: "+_keyCode);				
				Log.i(LOG_TAG, "keyAction: "+keyAction+" keyCode: "+keyCode+" Char: "+event.getCharacters());				
			}
			if(event.getCharacters()!=null&&!event.getCharacters().equals("测试")){
				_keyCodeTest = !_keyCodeTest;
			}

			return true;
		}

	    return super.dispatchKeyEvent(event);
	}
	private int _keyCode = 0;
	private boolean _keyCodeTest = false;

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
	
    private final static KeyEvent KEY_RIGHT_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT); 
    private final static KeyEvent KEY_RIGHT_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT);
    private final static KeyEvent KEY_LEFT_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT);
    private final static KeyEvent KEY_LEFT_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT);
    private final static KeyEvent KEY_DOWN_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
    private final static KeyEvent KEY_DOWN_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN);
    private final static KeyEvent KEY_UP_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
    private final static KeyEvent KEY_UP_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP);
    private final static float TRACKBALL_THRESHOLD = 0.4f; 
	private float accumulatedTrackballX = 0;
	private float accumulatedTrackballY = 0;
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

		int commandIndex = item.getItemId() - Menu.FIRST;
		List<AndroidCommandUI> commands = ui.getCommandsUI();
		CommandUI c = commands.get(commandIndex);

		if (c != null) {
			MIDletBridge.getMIDletAccess().getDisplayAccess().commandAction(c.getCommand(), da.getCurrent());
			return true;
		}

		return false;
	}
	
	// http://www.cnblogs.com/sw926/p/3208158.html
	class MyGesture extends SimpleOnGestureListener {

        // 触摸屏按下时立刻触发
		@Override
        public boolean onDown(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onDown");
            return super.onDown(e);
        }

        // 短按，触摸屏按下后片刻后抬起，会触发这个手势，如果迅速抬起则不会；强调的是没有松开或者拖动的状态，由一个ACTION_DOWN触发
        @Override
        public void onShowPress(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onShowPress");
        }

        // 抬起，手指离开触摸屏时触发(长按、滚动、滑动时，不会触发这个手势)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onSingleTapUp");
            return super.onSingleTapUp(e);
        }

        // 用户按下触摸屏，并拖动，由1个 ACTION_DOWN, 多个ACTION_MOVE触发
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            //android.util.Log.i(LOG_TAG, "onScroll");
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        // 长按，触摸屏按下后既不抬起也不移动，由多个 ACTION_DOWN触发
        @Override
        public void onLongPress(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onLongPress");
            // 2. 启动计时器
        	//android.util.Log.i(LOG_TAG, "config: Setting_LongPressOpen "+config.Setting_LongPressOpen);
            if(config.Setting_LongPressOpen){
            	android.util.Log.i(LOG_TAG, "config: Setting_LongPressTimeout "+config.Setting_LongPressTimeout);
            	android.util.Log.i(LOG_TAG, "runnable: postDelayed ");
            	handler.postDelayed(runnable, (long) (config.Setting_LongPressTimeout*1000));//每两秒执行一次runnable
            }
        }
    
        // 长按，触摸屏按下后既不抬起也不移动，由多个 ACTION_DOWN触发
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onDoubleTap");
            
            //android.util.Log.i(LOG_TAG, "config: FullscreenChange "+config.Screen_SwitchOnDoubleTap);      	
            if(config.Screen_SwitchOnDoubleTap){
	            windowFullscreen = !windowFullscreen;
	            LayoutParams params = null;
	            params  = getWindow().getAttributes();  
				if (windowFullscreen) {  
		            android.util.Log.i(LOG_TAG, "onDoubleTap: FullscreenChange "+windowFullscreen);      	
	                params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; 
	                getWindow().setAttributes(params);  
	                getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);                
	            } else {  
	                android.util.Log.i(LOG_TAG, "onDoubleTap: FullscreenChange "+windowFullscreen);      	
	                params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN); 
	                getWindow().setAttributes(params);  
	                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        		new Thread("WindowManager"){
        			@Override
        			public void run() {				
		        post(new Runnable() {
		            public void run() {
		                LayoutParams params = new WindowManager.LayoutParams();
		                params.x = windowFullscreen?statusBarHeight:0;
					    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
		            }
		        });
        			}
        		}.start();
	            } 
            }
			
        	return super.onDoubleTap(e);
        }

		/** 参数解释：
		// e1：第1个ACTION_DOWN MotionEvent
		// e2：最后一个ACTION_MOVE MotionEvent
		// velocityX：X轴上的移动速度，像素/秒
		// velocityY：Y轴上的移动速度，像素/秒
		// 触发条件 ： X轴的坐标位移大于FLING_MIN_DISTANCE，
		// 且移动速度大于FLING_MIN_VELOCITY个像素/秒
		*/
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
            //android.util.Log.i(LOG_TAG, "onFling");

            final int FLING_MIN_DISTANCE = 100;// X或者y轴上移动的距离(像素)
			final int FLING_MIN_VELOCITY = 200;// x或者y轴上的移动速度(像素/秒)

//			Device device = DeviceFactory.getDevice();
//			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
			KeyEvent event = null;
		
			if ((e1.getX() - e2.getX()) > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
				//Toast.makeText(MicroEmulator.this, "向左滑动", Toast.LENGTH_SHORT).show();
				event = MicroEmulator.KEY_RIGHT_DOWN_EVENT;
			}
			else if ((e2.getX() - e1.getX()) > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
				//Toast.makeText(MicroEmulator.this, "向右滑动", Toast.LENGTH_SHORT).show();
				event = MicroEmulator.KEY_DOWN_UP_EVENT;
			}

			if(event!=null){
				dispatchKeyEvent(event);
				//inputMethod.buttonPressed(keycode).buttonReleased(keycode);
			}
			return false;
		}
	}
	
	private GestureDetector gesture=new GestureDetector(new MyGesture ());

	Handler handler=new Handler(); 
	Runnable runnable=new Runnable() { 
	    @Override
	    public void run() { 
	        startActivityForResult(new Intent(MicroEmulator.this,SettingsActivity.class), REQ_SYSTEM_SETTINGS);
	    } 
	};

	@Override
	public boolean onTouch(final View paramView, MotionEvent event) {
		 String name = "";
		 switch (event.getAction()) {
		     case MotionEvent.ACTION_DOWN: {
		         name = "ACTION_DOWN";
		         break;
		     }
		     case MotionEvent.ACTION_MOVE: {
		         name = "ACTION_MOVE";
		         break;
		     }
		     case MotionEvent.ACTION_UP: {
		         name = "ACTION_UP";
		         break;
		     }
		 }   
		 switch (event.getAction()) {
		     case MotionEvent.ACTION_DOWN: {
		         break;
		     }
		     case MotionEvent.ACTION_MOVE:
		     case MotionEvent.ACTION_UP:
	            // 3. 停止计时器
	        	//android.util.Log.i(LOG_TAG, "runnable: removeCallbacks");
	            handler.removeCallbacks(runnable); 
		         break;
	     }
		 //android.util.Log.i(LOG_TAG, "onTouch Action" + name);
		 return gesture.onTouchEvent(event);
	}
	
	
	 //Settings设置界面返回的结果  
    int REQ_SYSTEM_SETTINGS=0;
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(requestCode == REQ_SYSTEM_SETTINGS)  
        {  
            //获取设置界面PreferenceActivity中各个Preference的值  
			setConfig(getPreferences(config,PreferenceManager.getDefaultSharedPreferences(this)));
		    //打印结果  
            Log.v(LOG_TAG, "onActivityResult");  
        }  
        else  
        {  
        	int a = Canvas.KEY_NUM0;
            //其他Intent返回的结果
        }  
    }  
}

