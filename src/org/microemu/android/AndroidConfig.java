/**
 *  MicroEmulator
 *  Copyright (C) 2011 Bartek Teodorczyk <barteo@gmail.com>
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
 *  @version $Id: AndroidConfig.java 2419 2010-09-17 09:47:12Z barteo@gmail.com $
 */

package org.microemu.android;

import java.lang.reflect.Field;
import java.util.Date;

import org.microemu.android.annotation.DisableView;
import org.microemu.android.annotation.Entries;
import org.microemu.android.annotation.Summary;
import org.microemu.android.annotation.Title;
import org.microemu.android.util.Tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

@Title("settings")
public class AndroidConfig {
	
	/**
	 * Font size definitions
	 */

	@Title("字体")
	public Font Font = new Font();
	
	@Title("字体")
	public class Font{
		@Title("小号字体")
		@Entries(value={8,10,12,14,16,18},names={"8","10","12","14","16","18"})
		public int SIZE_1SMALL = 12;
		
		@Title("中号字体")
		@Entries(value={12,14,16,18,20,22,24,26},names={"12","14","16","18","20","22","24","26"})
		public int SIZE_2MEDIUM = 16;
		
		@Title("大号字体")
		@Entries(value={16,18,20,22,24,26,28,30,32,34},
				names={"16","8","20","22","24","26","28","30","32","34"})
		public int SIZE_3LARGE = 20;
		
	}
	
	/**
	 * Area of the screen used for Canvas, values are in percentage units, eg. 100% = 1
	 */
	
	@DisableView()
	public static double CANVAS_AREA_LEFT = 0d;
	
	@DisableView()
	public static double CANVAS_AREA_TOP = 0d;
	
	@DisableView()
	public static double CANVAS_AREA_RIGHT = 1d;
	
	@DisableView()
	public static double CANVAS_AREA_BOTTOM = 1d;
	
	/**
	 * Use fixed display resolution and rescale if necessary
	 */
	
	@Title("屏幕固定大小")
	@DisableView()
    public static boolean ORIG_DISPLAY_FIXED = false;
    
	@DisableView()
    public static int ORIG_DISPLAY_WIDTH = 240;
    
	@DisableView()
    public static int ORIG_DISPLAY_HEIGHT = 320;
	@Title("默认全屏")
    public static boolean Screen_DefaultFull = true;
	@Title("双击切换全屏")
    public static boolean Screen_SwitchOnDoubleTap = true;
	@Title("通知栏显示时间")
	@Summary("单位秒")
	@Entries(value={1,2,3,4,5},names={"1秒","2秒","3秒","4秒","5秒"})
	public static int Screen_TimeoutForStart = 2;
	@Title("长按调出设置页面")
	public static boolean Setting_LongPressOpen = true;
	@Title("长按调出设置的时间")
	@Summary("单位秒")
	@Entries(value={1,2,3,4,5},names={"1秒","2秒","3秒","4秒","5秒"})
	public static int Setting_LongPressTimeout = 3;
    

}
