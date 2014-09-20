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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;

import org.android.annotation.DisableView;
import org.android.annotation.Entries;
import org.android.annotation.Title;
import org.android.media.MyMediaPlayer;
import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.MIDletEntry;
import org.microemu.android.device.AndroidDevice;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.android.device.ui.AndroidDisplayableUI;
import org.microemu.android.util.AndroidRecordStoreManager;
import org.microemu.app.Common;
import org.microemu.app.launcher.Launcher;
import org.microemu.app.util.MIDletSystemProperties;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.log.Logger;
import org.microemu.opm422.R;
import org.microemu.util.JadMidletEntry;
import org.microemu.util.JadProperties;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MEmulator extends MicroEmulator implements OnTouchListener {

	static final String menu_setting="设置";
//	static final String menu_scan_sdcard="scan sdcard";
//	static final String menu_apkTool="ApkTool";
	
	public static MyMediaPlayer player;
	public static Context context;
	Config _config = new Config();
    private boolean isFirstResume=true;
	int statusBarHeight = 0;

	private SharedPreferences pref;
	
	@Override
    public void onCreate(Bundle icicle) {
		//getPreferences(_config,getSharedPreferences(Config.Name, 0));
		pref=getSharedPreferences(Config.Name, 0);
		setConfig(getPreferences(_config,pref));
        config.FONT_SIZE_SMALL = _config.Font.SIZE_1SMALL;
        config.FONT_SIZE_MEDIUM = _config.Font.SIZE_2MEDIUM;
        config.FONT_SIZE_LARGE = _config.Font.SIZE_3LARGE;
       
		Drawable phoneCallIcon = getResources().getDrawable(android.R.drawable.stat_sys_phone_call);
		if (!windowFullscreen) {
			statusBarHeight = phoneCallIcon.getIntrinsicHeight();
		}

		super.onCreate(icicle);
	
		windowFullscreen=Config.Screen_DefaultFull;

    	if(android.os.Build.VERSION.SDK_INT <= 4)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
						   WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
       
		java.util.List<String> params = new ArrayList<String>();
        params.add("--usesystemclassloader");
        params.add("--quit");
        common = new Common(emulatorContext);
        String midletClassName=Launcher.class.getName();
        Vector<JadMidletEntry> midlets = null;
        try {
	        common.jad = new JadProperties();
	        common.jad.read(getAssets().open(Config.MANIFEST));
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
        midlet = common.initMIDlet(false);

        context = getApplication();
//        bgm.play(getApplication(), Uri.fromFile(new File("/sdcard/bgm.mp3")), true, AudioManager.STREAM_MUSIC);
    }

	@Override
	public void setContentView(View view) {
		//view.setBackgroundColor(Color.WHITE);
		super.setContentView(view);
        view.setOnTouchListener(this);
		
//		if(!windowFullscreen){
//            switchFullScreen(windowFullscreen,2);
//		}
	}
	
	@Override
    protected void onPause() {
        if(Config.Setting_PauseAppOnPause){
            super.onPause();
        }
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        
        setConfig(getPreferences(config,getSharedPreferences(Config.Name, 0)));
//        if(isFirstResume&&!windowFullscreen){
//        new Thread(new Runnable() {
//			public void run(){
//	        	postDelayed(new Runnable() {
//		            public void run() {
//		            	LayoutParams params = new WindowManager.LayoutParams();
//		            	params.x = windowFullscreen?0:statusBarHeight;
//					    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
//		            }
//		        },isFirstResume?2000:0);
//	    	}
//        },"switchFullScreen onResume");//.start();
//    	}
        switchFullScreen(Config.Screen_DefaultFull,isFirstResume?3:0);
    	isFirstResume = false;
    }
    
	int menuClickTime = 0;
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	boolean v = super.dispatchKeyEvent(event);
		int keyCode=event.getKeyCode(),keyAction=event.getAction(),unicodeChar=event.getUnicodeChar();
//
//		Log.i(LOG_TAG, "keyAction:"+keyAction+
//				" keyCode:"+keyCode+" menuClickTime:"+menuClickTime+
//				" Char:"+unicodeChar);

		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {return false;}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {return false;}
		if (da.getCurrent() == null) {return false;}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getDisplayableUI(da.getCurrent());
		if (ui == null) {return false;}
	
		if(keyAction==KeyEvent.ACTION_DOWN){
			if(keyCode==KeyEvent.KEYCODE_SEARCH){
			menuClickTime++;
			if(menuClickTime==3){
				Intent intent = new Intent();
				intent.setClass(MEmulator.this,SettingsActivity.class);
				startActivity(intent);menuClickTime=0;
			}else if(menuClickTime==1){
				new Thread("menuClickTime"){
					@Override
					public void run() {	
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						post(new Runnable() {
				            public void run() {
				            	menuClickTime=0;
				    			Log.i(LOG_TAG, "menuClickTime:"+menuClickTime);
				            }
				        });
					}
				}.start();
			}}
			
			if (keyCode == KeyEvent.KEYCODE_MENU){
			//windowFullscreen=false;
			//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				return false;
			}		
		}
				
		// support Num KeyEvent for opera mini mod
		int pressTime = 0;
		if(Config.Setting_SupportNumKey){
		if(keyCode==KeyEvent.KEYCODE_0){pressTime=10;}
		else if(keyCode==KeyEvent.KEYCODE_1)pressTime=21;
		else if(keyCode>=KeyEvent.KEYCODE_2&&keyCode<=KeyEvent.KEYCODE_6)pressTime=3;
		else if(keyCode==KeyEvent.KEYCODE_8)pressTime=3;
		else if(keyCode==KeyEvent.KEYCODE_7||keyCode==KeyEvent.KEYCODE_9)pressTime=4;
		}
		
		switch (keyCode) { 
		case KeyEvent.KEYCODE_DPAD_CENTER :	keyCode = -5;break;
		case KeyEvent.KEYCODE_DPAD_UP :		keyCode = -1;break;
		case KeyEvent.KEYCODE_DPAD_DOWN :	keyCode = -2;break;
		case KeyEvent.KEYCODE_DPAD_LEFT :	keyCode = -3;break;
		case KeyEvent.KEYCODE_DPAD_RIGHT :	keyCode = -4;break;
		case KeyEvent.KEYCODE_DEL :			keyCode = 127;break;
		default: String ch=event.getCharacters();
			if(ch!=null){if(ch.length()>0)keyCode=(int)ch.charAt(0);}else{if(unicodeChar!=0)keyCode=unicodeChar;else keyCode=-127-keyCode;}
		}
	
		if (ui instanceof AndroidCanvasUI) {
			Device device = DeviceFactory.getDevice();
			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
//			if(keyAction==KeyEvent.ACTION_DOWN)inputMethod.buttonPressed(keyCode);
//			else if(keyAction==KeyEvent.ACTION_UP)inputMethod.buttonReleased(keyCode);
			if(keyAction==KeyEvent.ACTION_MULTIPLE) {
//				inputMethod.buttonPressed(keyCode);
//				inputMethod.buttonReleased(keyCode);
				// support Chinese character
				if(event.getCharacters()!=null&&event.getCharacters().length()>1){
					for (int i = 1; i < event.getCharacters().length(); i++) {
						int secondKeyCode = event.getCharacters().charAt(i);
						inputMethod.buttonPressed(secondKeyCode);
						inputMethod.buttonReleased(secondKeyCode);
					}
				}
			}
			if(keyAction==KeyEvent.ACTION_UP&&pressTime>0){
				for (int ii = 0; ii < pressTime; ii++) {
					inputMethod.buttonPressed(keyCode);
					inputMethod.buttonReleased(keyCode);
				}
				// support input number twice 
				dispatchKeyEvent(new KeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT));
			}
			return true;
		}

	    return v;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return _onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
//		if(item.getTitle().equals(menu_scan_sdcard)){
//			showFileChooser();
//		}else if(item.getTitle().equals(menu_apkTool)){
//			try {
//				uri="file:///storage/sdcard0/download/duomi_3.6.0.jar";
//	            String dexUrl = uri.substring(uri.lastIndexOf("."),uri.length())+"dex";
//	            MIDletClassLoader midletClassLoader = common.createMIDletClassLoader(true);
////	            Common.Context = this;
////	            common.openMIDletUrl(uri, common.createMIDletClassLoader(true));
//	            midletClassLoader.addURL(new URL(dexUrl));
////				common.openMIDletUrl(uri);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			//startActivityForResult(new Intent(MEmulator.this,ApkToolActivity.class), REQ_SYSTEM_SETTINGS);
//		}else 
		if(item.getTitle().equals(menu_setting)){
	        startActivityForResult(new Intent(MEmulator.this,SettingsActivity.class),menu_setting.hashCode());
		}
		return result;
	}
	
    private final static KeyEvent KEY_RIGHT_DOWN_EVENT = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
    private final static KeyEvent KEY_DOWN_UP_EVENT = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN);
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
            //if(Config.Setting_LongPressOpen){
            	//android.util.Log.i(LOG_TAG, "config: Setting_LongPressTimeout "+config.Setting_LongPressTimeout);
            	//android.util.Log.i(LOG_TAG, "runnable: postDelayed ");
            	// delay begin after longPress, so dalay time must reduce the longpress time(about one second)
            	// so define the Setting_LongPressTimeout default value = 0
            	//handler.postDelayed(runnable, (long) (Config.Setting_LongPressTimeout*1000));//每两秒执行一次runnable
            //}
        }
    
        // 长按，触摸屏按下后既不抬起也不移动，由多个 ACTION_DOWN触发
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onDoubleTap");
            
            //android.util.Log.i(LOG_TAG, "config: FullscreenChange "+config.Screen_SwitchOnDoubleTap);      	
    		windowFullscreen = !windowFullscreen;
    		if(Config.Screen_SwitchOnDoubleTap){
    			switchFullScreen(windowFullscreen,0);
	    		Editor editor=pref.edit();
	    		editor.putBoolean("Screen_DefaultFull", windowFullscreen);
	    		editor.commit();
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
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //android.util.Log.i(LOG_TAG, "onFling");

            final int FLING_MIN_DISTANCE = 100;// X或者y轴上移动的距离(像素)
			final int FLING_MIN_VELOCITY = 200;// x或者y轴上的移动速度(像素/秒)

//			Device device = DeviceFactory.getDevice();
//			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
			KeyEvent event = null;
		
			if ((e1.getX() - e2.getX()) > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
				//Toast.makeText(MicroEmulator.this, "向左滑动", Toast.LENGTH_SHORT).show();
				event = MEmulator.KEY_RIGHT_DOWN_EVENT;
			}
			else if ((e2.getX() - e1.getX()) > FLING_MIN_DISTANCE
					&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
				//Toast.makeText(MicroEmulator.this, "向右滑动", Toast.LENGTH_SHORT).show();
				event = MEmulator.KEY_DOWN_UP_EVENT;
			}

			if(event!=null){
				dispatchKeyEvent(event);
				//inputMethod.buttonPressed(keycode).buttonReleased(keycode);
			}
			return false;
		}
	}
	
	private GestureDetector gesture=new GestureDetector(new MyGesture ());

//	Handler handler=new Handler(); 
//	Runnable runnable=new Runnable() { 
//	    @Override
//	    public void run() { 
//	        startActivityForResult(new Intent(MEmulator.this,SettingsActivity.class), REQ_SYSTEM_SETTINGS);
//	    } 
//	};

	@Override
	public boolean onTouch(final View paramView, MotionEvent event) {
//		 String name = "";
//		 switch (event.getAction()) {
//		     case MotionEvent.ACTION_DOWN: {
//		         name = "ACTION_DOWN";
//		         break;
//		     }
//		     case MotionEvent.ACTION_MOVE: {
//		         name = "ACTION_MOVE";
//		         break;
//		     }
//		     case MotionEvent.ACTION_UP: {
//		         name = "ACTION_UP";
//		         break;
//		     }
//		 }   
		 switch (event.getAction()) {
		     case MotionEvent.ACTION_DOWN: {
		         break;
		     }
		     case MotionEvent.ACTION_MOVE:
		     case MotionEvent.ACTION_UP:
	            // 3. 停止计时器
	        	//android.util.Log.i(LOG_TAG, "runnable: removeCallbacks");
	            //handler.removeCallbacks(runnable); 
		         break;
	     }
		 //android.util.Log.i(LOG_TAG, "onTouch Action" + name);
		 return gesture.onTouchEvent(event);
	}
	
	public void switchFullScreen(final boolean windowFullscreen, final int i) {
            android.util.Log.i(LOG_TAG, "switchFullScreen "+windowFullscreen+" "+i);      	
        	if(android.os.Build.VERSION.SDK_INT <= 4){
    		if (Config.Screen_TransparentStatusBar)
    			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    		else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        	}

            final LayoutParams params = getWindow().getAttributes();  
			if (windowFullscreen) {  
                params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; 
                getWindow().setAttributes(params);  
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);                
            } else {  
    		new Thread("WindowManager"){
    			@Override
    			public void run() {	
    				try {Thread.sleep(1000*i);} catch (InterruptedException e) {}
	        post(new Runnable() {
	            public void run() {
	                params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN); 
	                getWindow().setAttributes(params);  
	                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	                LayoutParams params = new WindowManager.LayoutParams();
	                params.x = windowFullscreen?statusBarHeight:0;
				    getWindowManager().updateViewLayout((View) getWindow().getDecorView(), params);
	            }
	        });
    			}
    		}.start();
        }
	}

	private AlertDialog menuDialog;// menu菜单Dialog
	private GridView menuGrid;//meunu菜单的卡片布局
	private View menuView;//一个用来填充的view
	private Menu menu;
	private MenuAdapter menuAdapter;

	//初如化卡片布局菜单
	public void creatMenuGird()
	{
		// 设置自定义menu菜单
		if (menuView == null){
			menuView = View.inflate(this, R.layout.gridview_meun, null);//用自定义的XML来填充一个 view
			menuDialog = new AlertDialog.Builder(this).create();// 创建AlertDialog
//			Window dialogWindow = menuDialog.getWindow();
//	        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//	        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
//	        lp.x = 0; // 新位置X坐标
//	        lp.y = display.getHeight()-200; // 新位置Y坐标
//	        lp.width = display.getWidth(); // 宽度
//	        lp.height = 300; // 高度
////	        lp.alpha = 0.7f; // 透明度
//	        dialogWindow.setAttributes(lp);
			//设置窗口的大小
			//dialog.getWindow().setLayout(300, 200);
			//或者这样设置大小   
			//WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			//params.width = 300;
			//params.height = 200;
			//dialog.getWindow().setAttributes(params);
	         
			menuDialog.setView(menuView);//把这个view设置到dialog上面去
//			menuDialog.setOnKeyListener(new ClickMenuListener());//监听键盘按下Menu
			menuGrid = (GridView) menuView.findViewById(R.id.gridview);
			
			ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < menu.size(); i++)
			{
				HashMap<String, Object> map = new HashMap<String, Object>();
				//map.put("itemImage", imageResourceArray[i]);
				map.put("itemText", menu.getItem(i));
				data.add(map);
			}
			menuAdapter = new MenuAdapter(this, data, R.layout.gridview_menu_item, 
					new String[]{ "itemText" }, 
					new int[]{ R.id.item_text });
			
			menuGrid.setAdapter(menuAdapter);//为menuGrid添加适配器
			menuGrid.setOnItemClickListener(new MenuItemClickListener());//监听你点了那一项
			menuDialog.setCanceledOnTouchOutside(true);
		}
	}
	//创建菜单项
	public boolean _onPrepareOptionsMenu(Menu pmenu)
	{
		boolean v= super.onPrepareOptionsMenu(pmenu);
		pmenu.add(menu_setting);
		this.menu = pmenu;
	    creatMenuGird();//初如化网格布局菜单
	    menuAdapter.notifyDataSetChanged();
	    return v;
	}
	
	//拦截系统默认菜单
	@Override
	public boolean onMenuOpened(int featureId, Menu menu){
		if(Config.Setting_CusomMenu)
			if (menuDialog == null)menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
			else menuDialog.show();
		//返回true为系统菜单，返回false为用户自己定义菜单
		return !Config.Setting_CusomMenu;
	}

	//构建一个适配器
	class MenuAdapter extends SimpleAdapter{
		public MenuAdapter(Context context,List<? extends Map<String, ?>> data, int resource,String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItem item = menu.getItem(position);
			View v = super.getView(position, convertView, parent);
			TextView item_text = (TextView) v.findViewById(R.id.item_text);
			if(!item.isVisible()){
				item_text.setTextColor(Color.DKGRAY);
			}else item_text.setTextColor(Color.WHITE);
			return v;
		}
		
	}
	//监听你点了那一项菜单
	private class MenuItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			MenuItem item = menu.getItem(arg2);
			if(item.isVisible()){onOptionsItemSelected(item);
				menuDialog.dismiss();//要先把东西干掉，不然会发生异常
			}
		}
	}
	
	public <T> T getPreferences(T t,SharedPreferences preferences) {
		Field[] fields = t.getClass().getFields();
		for (Field field : fields) {
			DisableView disEnabled = field.getAnnotation(DisableView.class);
			if(disEnabled!=null)continue;
			Class<?> type = field.getType(); 
			String fieldName=field.getName(); 
			Title titleAnn = field.getAnnotation(Title.class);
			if(titleAnn==null)continue;
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
						Float value = preferences.getFloat(fieldName, Float.parseFloat(field.get(t).toString()));
						field.set(t, value);
					}else continue;
				}
				else {
					// ListPreference 保存的只能是 String
					String value = preferences.getString(fieldName, field.get(t).toString());
					field.set(t, Integer.parseInt(value));
				}
			} catch (Exception e) {
				Log.e("Exception","Exception : "+fieldName);
				e.printStackTrace();
			}
		}
		return t;
	}

	public boolean _platformRequest(String url) throws ConnectionNotFoundException
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
			
			onPause();
		} catch (ActivityNotFoundException e) {
			throw new ConnectionNotFoundException(e.getMessage());
		}
		return true;
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


	static int count=0;

    int FILE_SELECT_CODE=1;
	private Intent intent;
    /** 调用文件选择软件来选择文件 **/  
    private void showFileChooser() {  
        intent = new Intent(Intent.ACTION_GET_CONTENT);  
        intent.setType("*/*");  
        intent.addCategory(Intent.CATEGORY_OPENABLE);  
        try {  
			startActivityForResult(Intent.createChooser(intent, "请选择一个文件"), FILE_SELECT_CODE);  
        } catch (ActivityNotFoundException ex) {  
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();  
        }  
    } 
    @Override  
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
    	if(requestCode==menu_setting.hashCode()){
            setConfig(getPreferences(config,getSharedPreferences(Config.Name, 0)));
                switchFullScreen(Config.Screen_DefaultFull,0);
    	    //if(Config.Screen_DefaultFull!=windowFullscreen)
    	}else{
			showFileChooserResult(requestCode, resultCode, data);
    	}
    }  
    /** 根据返回选择的文件，来进行上传操作 **/  
    public void showFileChooserResult(int requestCode, int resultCode, Intent data) {  
//		if (requestCode == FILE_SELECT_CODE) { 
//        	if (resultCode == Activity.RESULT_OK) { 
//	        	String msg = "";
//	        	if(data==null||data.getData()==null)msg="not select jar file";
//	        	else {
//	        		// file:///storage/sdcard1/Java/mini8.jar
//		            uri = data.getData().toString();  
//	        		if(uri.toLowerCase().endsWith(".jar")){
//					String fileName = uri.substring(uri.lastIndexOf("/") + 1);  
//					msg = uri+" "+fileName;
//	        		// file:///storage/sdcard1/Java/mini8.jar
//					final String command1 = new String(" sh ") 
//					+ getExternalFilesDir(null) +"/dex2jar/d2j-dex2jar.sh " 
//					+ uri + " " + uri.substring(0, uri.length()-4) + ".dex";
//	        		}else if(uri.toLowerCase().endsWith(".dex")){}
//
//	        	}
//	            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();  
//        	}
//        }  
    } 

    
    

//    public static void commandAction(Command paramCommand, Displayable paramDisplayable){
//    	Log.i("commandAction.Displayable ", "commandAction.Displayable "+paramCommand.getLabel()+" "+paramCommand.getLongLabel());
//    } 
//    public static void commandAction(Command paramCommand,Item paramItem){
//    	Log.i("commandAction.Item ", "commandAction.Item "+paramCommand.getLabel()+" "+paramCommand.getLongLabel());
//    }  
//    public static void getResourceAsStream(String string){
//    	if("l".equals(string))
//    		Debug.waitForDebugger();
//    	Log.i(LOG_TAG, "getResourceAsStream "+string);
//    } 
//    public static void a_String_ar(String string){
////    	if("l".equals(string))
////    		Debug.waitForDebugger();
//    	Log.i(LOG_TAG, "a_String_ar "+string);
//    }
//    public static void a_Lar(String string){
////    	if("l".equals(string))
////    		Debug.waitForDebugger();
//    	Log.i(LOG_TAG, "a_Lar "+string);
//    }  
//    public static void bz(Object b,String string){
//    	Debug.waitForDebugger();
//		//Log.i(LOG_TAG, "bz "+b.getClass().getSimpleName());
//		try {
//			Class<?> bzClass = Class.forName("bz");
//			for (Field field : bzClass.getFields()) {
//				Object v = field.get(null);
//				if(v instanceof String[])
//					v = Arrays.toString((String[])v);
//				//Log.i(LOG_TAG, "bz name: "+field.getName() +" value:"+v);
//			}
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//    
//    public static void bz(String string){
//		if("复制全部".equals(string)){
//    	Debug.waitForDebugger();
//		Log.i(LOG_TAG, "bz "+string);
//		}
//    }
//    public static void keyPressed(final int n){
//    	Log.i(LOG_TAG, "keyPressed "+n);
//    } 
//    public static void getKeyName(String name,final int n){
//    	Log.i(LOG_TAG, "getKeyName "+n+" "+name);
//    } 
//    public static void pointerReleased(final int n, int b){
//    	Log.i(LOG_TAG, "pointerReleased "+n +" "+b);
//    } 
//	public static void a(Graphics paramGraphics, int p1, int p2, int p3){
////    	Log.i(LOG_TAG, "Graphics "+p1 +" "+p2+" "+p3);		
//	}
//	public static void a(){
////    	Log.i(LOG_TAG, "Graphics ");		
//	}
//	public static void aInputStream(InputStream a){
////    	Log.i(LOG_TAG, "Graphics ");		
//	}	

}


