package org.microemu.android;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.android.annotation.DisableView;
import org.android.annotation.Entries;
import org.android.annotation.Summary;
import org.android.annotation.Title;
import org.android.annotation.Type;
import org.android.util.Tools;
import org.android.view.EditListPreference;
import org.microemu.opm422.R;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	SharedPreferences prefs;
	PreferenceScreen preferenceScreen;
	String language = "Config_Language";

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		getPreferenceManager().setSharedPreferencesName(Config.Name);
		prefs = getSharedPreferences(Config.Name, 0);
		preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
		setPreferenceScreen(preferenceScreen);
		
//		ListPreference langPreference = new ListPreference(this);
//		preferenceScreen.addItemFromInflater(langPreference);
//		langPreference.setEntries(R.array.lang_name);
//		langPreference.setEntryValues(R.array.lang_value);
//		langPreference.setKey(language);
//		langPreference.setTitle(R.string.Setting_Language);
//		langPreference.setEntries(Tools.add(langPreference.getEntries(),getText(R.string.Setting_Language_Default)));
//		langPreference.setEntryValues(Tools.add(langPreference.getEntryValues(),Locale.getDefault().toString()));
//		langPreference.setOnPreferenceChangeListener(this);
//		String summary = getText(R.string.Setting_Summary_Prefix).toString();
//		summary += Locale.getDefault().toString();
//		summary=summary+" "+getText(R.string.Setting_Summary_suffix).toString()+prefs.getAll().get(language);
//		langPreference.setSummary(summary);
//		langPreference.setValue(prefs.getString(language,Locale.getDefault().toString()));
		
		AddPreference(new Config());
	}
	
	public <T> void AddPreference(T t) {
		Class<?> clazz = t.getClass();
		Field[] fields = clazz .getFields();
		PreferenceCategory category = new PreferenceCategory(this);
		preferenceScreen.addPreference(category);
		String title = Tools.getName(clazz);
		Title titleAnn = clazz.getAnnotation(Title.class);
		if(titleAnn!=null)title=getText(titleAnn.value()).toString();
		else return;
		category.setTitle(title);
		for (Field field : fields) {
			DisableView disEnabled = field.getAnnotation(DisableView.class);
			if(disEnabled!=null)continue;
			String fieldName=field.getName(); 
			Object defaultValue = null;
			Class<?> type = field.getType(); 
			try {
				Preference preference = null;
				defaultValue=field.get(t);
				if(type.getPackage()!=null&&!type.getPackage().getName().startsWith("java.lang"))
					AddPreference(defaultValue);
				Entries entries = field.getAnnotation(Entries.class);
				if(entries==null){
					if(defaultValue instanceof String){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else if(type.equals(int.class)||type.equals(byte.class)){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else if(type.equals(Integer.class)||type.equals(Byte.class)){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else if(type.equals(boolean.class)){
						preference = new CheckBoxPreference(this);
						((CheckBoxPreference) preference).setChecked((Boolean)defaultValue);
					}else if(type.equals(Boolean.class)){
						preference = new CheckBoxPreference(this);
						((CheckBoxPreference) preference).setChecked((Boolean)defaultValue);
					}else if(type.equals(Date.class)){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else if(type.equals(Float.class)||type.equals(float.class)){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else if(type.equals(Double.class)||type.equals(double.class)){
						preference = new EditTextPreference(this);
						((EditTextPreference) preference).setText(defaultValue.toString());
					}else continue;
				}
				else {
					// ListPreference 保存的只能是 String
					preference = new EditListPreference(this);
					((EditListPreference) preference).setEntries(entries.nameId());
					((EditListPreference) preference).setEntryValues(entries.valueId());
					((EditListPreference) preference).setEntries(entries.names());
					((EditListPreference) preference).setEntryValues(Tools.Ints2Strings(entries.value()));
					CharSequence[] vs = ((ListPreference) preference).getEntryValues();
//					System.out.println(Arrays.toString(vs));
//					((ListPreference) preference).setEntryValues(R.array.FontSIZE_LARGE_Value);
//					vs = ((ListPreference) preference).getEntryValues();
//					System.out.println(Arrays.toString(vs));
					Type inpuType = field.getAnnotation(Type.class);
					if(inpuType!=null)((EditListPreference) preference).inputType=inpuType.value();
					if(Arrays.binarySearch(vs, defaultValue.toString())<0)
						((ListPreference) preference).setValueIndex(0);
					else ((ListPreference) preference).setValueIndex(
							Tools.getIndex(vs,defaultValue.toString()));
				}
				
				preference.setKey(fieldName);
				titleAnn = field.getAnnotation(Title.class);
				if(titleAnn!=null)title=getText(titleAnn.value()).toString();
				else continue;
				//else title = fieldName;
				preference.setTitle(title);
				String summary = getText(R.string.Setting_Summary_Prefix).toString();
				//if(entries!=null)defaultValue = entries.value()[(Integer)defaultValue];
				summary += defaultValue;
				Summary summaryAnn = field.getAnnotation(Summary.class);
				if(summaryAnn!=null)summary=getText(summaryAnn.value()).toString() +" "+summary;
				summary=summary+" "+getText(R.string.Setting_Summary_suffix).toString()+prefs.getAll().get(fieldName);
				preference.setSummary(summary);
				category.addPreference(preference);
				
				preference.setOnPreferenceChangeListener(this);
			} catch (Exception e) {
				Log.i(MicroEmulator.LOG_TAG, "Exception: "+fieldName+" "+defaultValue+" "+title+" "+type+" "+e.getMessage());
			}     
		}
	}

	@Override
	public boolean onPreferenceChange(Preference p,Object o) {
		if(language.equals(p.getKey())){
			Locale l=Locale.getDefault();
			if(!(o==null||"".equals(o.toString().trim())))
				l=new Locale(o.toString());
			if(l!=null){
				updateLanguage(l);
			}
		}

		String summary = (String) p.getSummary();
		summary = summary.substring(0,summary.indexOf("当前为")+3)+" "+o;
		p.setSummary(summary);
		//如果返回false表示不允许被改变  true表示允许改变
		return true;
	}
	private void updateLanguage(Locale locale) {
		Log.d("ANDROID_LAB", locale.toString());
//		IActivityManager iActMag = ActivityManagerNative.getDefault();
//		try {
//			Configuration config = iActMag.getConfiguration();
//			config.locale = locale;
//			// 此处需要声明权限:android.permission.CHANGE_CONFIGURATION
//			// 会重新调用 onCreate();
//			iActMag.updateConfiguration(config);
//		} catch (RemoteException e) {e.printStackTrace();}
		
		Resources res = getResources();  
		Configuration config = res.getConfiguration();  
		config.locale = locale;  
		DisplayMetrics dm = res.getDisplayMetrics();  
		res.updateConfiguration(config, dm); 
		
	}
}
