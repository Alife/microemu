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
import org.android.annotation.Type;
import org.microemu.opm422.R;

import android.text.InputType;

@Title(R.string.Setting)
public class Config extends AndroidConfig {

	@DisableView()
    public static final String MANIFEST = "META-INF/MANIFEST.MF";
	@DisableView()
    public static final String Name = "AndroidConfig";
	
	@Title(R.string.Config_Font_Title)
	public Font Font = new Font();
	
	@Title(R.string.Config_Font_Title)
	public class Font{
		@Type(InputType.TYPE_CLASS_NUMBER)
		@Title(R.string.Config_SIZE_SMALL_Title)
		@Entries(value={8,10,12,14,16,18},names={"8","10","12","14","16","18"}
			,nameId = R.array.FontSIZE_SMALL_Name, valueId = R.array.FontSIZE_SMALL_Value)
		public int SIZE_1SMALL = FONT_SIZE_SMALL;
		
		@Type(InputType.TYPE_CLASS_NUMBER)
		@Title(R.string.Config_SIZE_MEDIUM_Title)
		@Entries(value={12,14,16,18,20,22,24,26},names={"12","14","16","18","20","22","24","26"}
			,nameId = R.array.FontSIZE_MEDIUM_Name, valueId = R.array.FontSIZE_MEDIUM_Value)
		public int SIZE_2MEDIUM = FONT_SIZE_MEDIUM;
		
		@Type(InputType.TYPE_CLASS_NUMBER)
		@Title(R.string.Config_SIZE_LARGE_Title)
		@Entries(value={16,18,20,22,24,26,28,30,32,34}
			,names={"16","18","20","22","24","26","28","30","32","34"}
			,nameId = R.array.FontSIZE_LARGE_Name, valueId = R.array.FontSIZE_LARGE_Value)
		public int SIZE_3LARGE = FONT_SIZE_LARGE;
		
	}
	
	@Title(R.string.Config_DefaultFull_Title)
	@Summary(R.string.Config_DefaultFull_Summary)
	public static boolean Screen_DefaultFull = true;
	@Title(R.string.Config_SwitchOnDoubleTap_Title)
    public static boolean Screen_SwitchOnDoubleTap = false;
	@Title(R.string.Config_SupportNumKey_Title)
	public static boolean Setting_SupportNumKey = false;
	//@Title("OnPause 时暂停 app")
	@DisableView()
	public static boolean Setting_PauseAppOnPause = true;
	@Title(R.string.Config_TransparentStatusBar_Title)
//	@Summary("4.0 以上无效")
	public static boolean Screen_TransparentStatusBar = false;
	@Title(R.string.Config_CusomMenu_Title)
	public static boolean Setting_CusomMenu = false;
    
}
