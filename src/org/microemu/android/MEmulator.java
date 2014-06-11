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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.android.device.ui.AndroidDisplayableUI;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.opm422.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AsyncPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MEmulator extends MicroEmulator implements OnTouchListener {

	static final String menu_setting="setting";
	static final String menu_scan_sdcard="scan sdcard";
	static final String menu_apkTool="ApkTool";
	
	public static AsyncPlayer bgm = new AsyncPlayer("AsyncPlayer");
	public static Context context;
	
	@Override
    public void onCreate(Bundle icicle) {
		setConfig(getPreferences(config,getSharedPreferences(AndroidConfig.Name, 0)));
        super.onCreate(icicle);
       
        midlet = common.initMIDlet(false);
    	
        context = getApplication();
//        bgm.play(getApplication(), Uri.fromFile(new File("/sdcard/bgm.mp3")), true, AudioManager.STREAM_MUSIC);
    }

	@Override
	public void setContentView(View view) {
		//view.setBackgroundColor(Color.WHITE);
		super.setContentView(view);
        view.setOnTouchListener(this);
		
		if(!windowFullscreen){
            switchFullScreen(windowFullscreen,2);
		}
	}
	
	@Override
    protected void onPause() {
        if(AndroidConfig.Setting_PauseAppOnPause){
            super.onPause();
        }
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        setConfig(getPreferences(config,getSharedPreferences(AndroidConfig.Name, 0)));
	    
        new Thread(new Runnable() {

            public void run()
            {
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
    
	int menuClickTime = 0;
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	boolean v = super.dispatchKeyEvent(event);
		int keyCode=event.getKeyCode(),keyAction=event.getAction(),unicodeChar=event.getUnicodeChar();

		Log.i(LOG_TAG, "keyAction:"+keyAction+
				" keyCode:"+keyCode+" menuClickTime:"+menuClickTime+
				" Char:"+unicodeChar);

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
		if(AndroidConfig.Setting_SupportNumKey){
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
			if(keyAction==KeyEvent.ACTION_DOWN)inputMethod.buttonPressed(keyCode);
			else if(keyAction==KeyEvent.ACTION_UP)inputMethod.buttonReleased(keyCode);
			else { // keyAction==KeyEvent.ACTION_MULTIPLE
				inputMethod.buttonPressed(keyCode);
				inputMethod.buttonReleased(keyCode);
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
		if(item.getTitle().equals(menu_scan_sdcard)){
			showFileChooser();
		}else if(item.getTitle().equals(menu_apkTool)){
			startActivityForResult(new Intent(MEmulator.this,MEmulator.class), REQ_SYSTEM_SETTINGS);
		}else if(item.getTitle().equals(menu_setting)){
	        startActivityForResult(new Intent(MEmulator.this,SettingsActivity.class), REQ_SYSTEM_SETTINGS);
		}

		return result;
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
            if(AndroidConfig.Setting_LongPressOpen){
            	//android.util.Log.i(LOG_TAG, "config: Setting_LongPressTimeout "+config.Setting_LongPressTimeout);
            	//android.util.Log.i(LOG_TAG, "runnable: postDelayed ");
            	// delay begin after longPress, so dalay time must reduce the longpress time(about one second)
            	// so define the Setting_LongPressTimeout default value = 0
            	handler.postDelayed(runnable, (long) (AndroidConfig.Setting_LongPressTimeout*1000));//每两秒执行一次runnable
            }
        }
    
        // 长按，触摸屏按下后既不抬起也不移动，由多个 ACTION_DOWN触发
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //android.util.Log.i(LOG_TAG, "onDoubleTap");
            
            //android.util.Log.i(LOG_TAG, "config: FullscreenChange "+config.Screen_SwitchOnDoubleTap);      	
    		windowFullscreen = !windowFullscreen;
            switchFullScreen(windowFullscreen,0);
			
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

	Handler handler=new Handler(); 
	Runnable runnable=new Runnable() { 
	    @Override
	    public void run() { 
	        startActivityForResult(new Intent(MEmulator.this,SettingsActivity.class), REQ_SYSTEM_SETTINGS);
	    } 
	};

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
	            handler.removeCallbacks(runnable); 
		         break;
	     }
		 //android.util.Log.i(LOG_TAG, "onTouch Action" + name);
		 return gesture.onTouchEvent(event);
	}
	
	public void switchFullScreen(final boolean windowFullscreen, final int i) {
		if(AndroidConfig.Screen_SwitchOnDoubleTap){
        	if(android.os.Build.VERSION.SDK_INT <= 4){
    		if (AndroidConfig.Screen_TransparentStatusBar)
    			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    		else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        	}

            final LayoutParams params = getWindow().getAttributes();  
			if (windowFullscreen) {  
	            android.util.Log.i(LOG_TAG, "onDoubleTap: FullscreenChange "+windowFullscreen);      	
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
	                android.util.Log.i(LOG_TAG, "onDoubleTap: FullscreenChange "+windowFullscreen);      	
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
					new int[]{  R.id.item_text });
			
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
//		pmenu.add(menu_scan_sdcard);
//		pmenu.add(menu_apkTool);
		this.menu = pmenu;
	    creatMenuGird();//初如化网格布局菜单
	    menuAdapter.notifyDataSetChanged();
	    return v;
	}
	
	//拦截系统默认菜单
	@Override
	public boolean onMenuOpened(int featureId, Menu menu){
		if (menuDialog == null)menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		else menuDialog.show();
		//返回true为系统菜单，返回false为用户自己定义菜单
		return false;
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

	//Settings设置界面返回的结果  
    int REQ_SYSTEM_SETTINGS=0;
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
    /** 根据返回选择的文件，来进行上传操作 **/  
    @Override  
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(requestCode == REQ_SYSTEM_SETTINGS){  
            //获取设置界面PreferenceActivity中各个Preference的值  
			setConfig(getPreferences(config,PreferenceManager.getDefaultSharedPreferences(this)));
            Log.v(LOG_TAG, "onActivityResult");  
        }else if (requestCode == FILE_SELECT_CODE) { 
        	if (resultCode == Activity.RESULT_OK) { 
	        	String msg = "";
	        	if(data==null||data.getData()==null)msg="not select jar file";
	        	else {
	        		// file:///storage/sdcard1/Java/mini8.jar
		            uri = data.getData().toString();  
					Log.i(LOG_TAG, "url " + uri);  
					String fileName = uri.substring(uri.lastIndexOf("/") + 1);  
					msg = uri+" "+fileName;
					final	String command1 = new String(" sh /sdcard/apktool/apktool.sh d -f -r ") 
					+ uri + " " + uri.substring(0, uri.length()-4) + "_src";
					//threadWork(this,getString(R.string.decompiling),command1,3);
	        	}
	            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();  
        	}
        }  
        super.onActivityResult(requestCode, resultCode, data);  
    }  
    
	static int count=0;
	public String uri;
	ProgressDialog myDialog;
	MyHandler myHandler = new MyHandler();
	class MyHandler extends Handler {	
		public void doWork(String str,final Bundle b){
			if(b.getBoolean("isTemp")){
				myDialog.setMessage(b.getString("op"));
			}else{
			SharedPreferences settings = getSharedPreferences("Settings", MODE_PRIVATE);
			if(settings.getInt("Vib", 0 ) != 0){
				Vibrator v = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
				v.vibrate(new long[]{0,200,100,200},-1);
			}
			if(settings.getInt("Noti", 0 ) != 0){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.ic_launcher,getString(R.string.op_done),System.currentTimeMillis());
				Context context = getApplicationContext(); 
				CharSequence contentTitle = b.getString("filename"); 
				CharSequence contentText =  getString(R.string.op_done); 
				Intent notificationIntent = MEmulator.this.getIntent();
				PendingIntent contentIntent = PendingIntent.getActivity(MEmulator.this,0,notificationIntent,0);
				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);	
				notification.flags |= Notification.FLAG_AUTO_CANCEL;				
				mNotificationManager.notify(count++,notification);				
			} 	
			myDialog.dismiss();
			Toast.makeText(MEmulator.this, str,Toast.LENGTH_LONG).show();
			AlertDialog.Builder b1 = new AlertDialog.Builder(MEmulator.this);
			String tmp_str = b.getString("filename")+"\n"+ getString(R.string.cost_time);
			
			long time = (System.currentTimeMillis() - b.getLong("time"))/1000;
			if(time > 3600){
				tmp_str += Integer.toString((int) (time/3600)) + getString(R.string.hour) + Integer.toString((int) (time%3600)/60) +
						getString(R.string.minute) + Integer.toString((int) (time%60)) + getString(R.string.second);
			}
			else if(time > 60){
				tmp_str +=  Integer.toString((int) (time%3600)/60) +
						getString(R.string.minute) + Integer.toString((int) (time%60)) + getString(R.string.second);
			}
			else{
				tmp_str +=  Integer.toString((int) time) + getString(R.string.second);
			}
			b1.setTitle(tmp_str)
			.setMessage(b.getString("output"))
			.setPositiveButton(getString(R.string.ok), null)
			.setNeutralButton((getString(R.string.copy)),
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					cmb.setText(b.getString("output"));
				}
			}).create().show();
//			currentFiles = currentParent.listFiles();
//			inflateListView(currentFiles);
		}
		}
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			final Bundle b = msg.getData();
			switch (b.getInt("what")) {
			case 0:
				doWork(getString(R.string.decompile_all_finish),b);
				break;
			case 1:
				doWork(getString(R.string.sign_finish),b);
				break;
			case 2:
				doWork(getString(R.string.recompile_finish),b);
				break;
			case 3:
				doWork(getString(R.string.decompile_dex_finish),b);
				break;
			case 4:
				doWork(getString(R.string.decompile_res_finish),b);
				break;
			case 5:
				doWork(getString(R.string.decompile_odex_finish),b);
				break;
			case 6:
				doWork(getString(R.string.op_done),b);
				break;
			case 7:
				doWork(getString(R.string.import_finish),b);
				break;
			case 8:
				doWork(getString(R.string.align_finish),b);
				break;
			case 9:
				doWork(getString(R.string.add_finish),b);
				break;
			case 10:
				doWork(getString(R.string.delete_finish),b);
				break;
			}
		}
	}

}

