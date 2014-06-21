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

import org.android.annotation.DisableView;
import org.android.annotation.Entries;
import org.android.annotation.Summary;
import org.android.annotation.Title;

@Title("settings")
public class Config extends AndroidConfig {

	@DisableView()
    public static final String MANIFEST = "META-INF/MANIFEST.MF";
	@DisableView()
    public static final String Name = "AndroidConfig";
	
	@Title("字体")
	public Font Font = new Font();
	
	@Title("字体")
	public class Font{
		@Title("小号字体")
		@Entries(value={8,10,12,14,16,18},names={"8","10","12","14","16","18"})
		public int SIZE_1SMALL = FONT_SIZE_SMALL;
		
		@Title("中号字体")
		@Entries(value={12,14,16,18,20,22,24,26},names={"12","14","16","18","20","22","24","26"})
		public int SIZE_2MEDIUM = FONT_SIZE_MEDIUM;
		
		@Title("大号字体")
		@Entries(value={16,18,20,22,24,26,28,30,32,34},
				names={"16","18","20","22","24","26","28","30","32","34"})
		public int SIZE_3LARGE = FONT_SIZE_LARGE;
		
	}
	
	@Title("默认全屏")
	@Summary("仅启动时生效。")
	public static boolean Screen_DefaultFull = true;
	@Title("双击切换全屏")
    public static boolean Screen_SwitchOnDoubleTap = false;
//	@Title("长按调出设置页面")
//	public static boolean Setting_LongPressOpen = false;
//	@Title("长按调出设置的时间")
//	@Summary("单位秒")
//	@Entries(value={1,2,3,4,5},names={"1秒","2秒","3秒","4秒","5秒"})
//	public static int Setting_LongPressTimeout = 1;
	@Title("直输界面启用数字键")
	public static boolean Setting_SupportNumKey = false;
	//@Title("OnPause 时暂停 app")
	@DisableView()
	public static boolean Setting_PauseAppOnPause = true;
	@Title("透明通知栏")
	@Summary("4.0 以上无效")
	public static boolean Screen_TransparentStatusBar = false;
	@Title("自定义菜单样式")
	public static boolean Setting_CusomMenu = false;
    
}
