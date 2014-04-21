package org.microemu.android;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import org.android.annotation.DisableView;
import org.android.annotation.Entries;
import org.android.annotation.Summary;
import org.android.annotation.Title;
import org.android.util.Tools;
import org.microemu.opm422.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.RelativeLayout;

public class SettingsActivity extends PreferenceActivity {
	RelativeLayout panel;
	SharedPreferences prefs;
	PreferenceScreen preferenceScreen;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		panel = new RelativeLayout(this);
		addPreferencesFromResource(R.xml.settings);
		preferenceScreen = getPreferenceScreen();
		preferenceScreen.removeAll();
		
		AddPreference(new AndroidConfig());
	}
	
	public <T> void AddPreference(T t) {
		Class<?> clazz = t.getClass();
		Field[] fields = clazz .getFields();
		PreferenceCategory category = new PreferenceCategory(this);
		preferenceScreen.addPreference(category);
		String title = Tools.getName(clazz);
		Title titleAnn = clazz.getAnnotation(Title.class);
		if(titleAnn!=null)title=titleAnn.value();
		category.setTitle(title );
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
					preference = new ListPreference(this);
					((ListPreference) preference).setEntries(entries.names());
					((ListPreference) preference).setEntryValues(Tools.Ints2Strings(entries.value()));
					if(Arrays.binarySearch(Tools.Ints2Strings(entries.value()), defaultValue.toString())<0)
						((ListPreference) preference).setValueIndex(0);
					else ((ListPreference) preference).setValueIndex(
							Tools.getIndex(entries.value(),Integer.parseInt(defaultValue.toString())));
				}
				
				preference.setKey(fieldName);
				titleAnn = field.getAnnotation(Title.class);
				if(titleAnn!=null)title=titleAnn.value();
				else title = fieldName;
				preference.setTitle(title);
				String summary = "默认为 ";
				//if(entries!=null)defaultValue = entries.value()[(Integer)defaultValue];
				summary += defaultValue;
				Summary summaryAnn = field.getAnnotation(Summary.class);
				if(summaryAnn!=null)summary=summaryAnn.value() +" "+summary;
				summary=summary+" 当前为 "+prefs.getAll().get(fieldName);
				preference.setSummary(summary);
				category.addPreference(preference);
				
				preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

					@Override
					public boolean onPreferenceChange(
							Preference preference, Object paramObject) {
						String summary = (String) preference.getSummary();
						summary = summary.substring(0,summary.indexOf("当前为")+3)+" "+paramObject;
						preference.setSummary(summary);
						//如果返回false表示不允许被改变  true表示允许改变
						return true;
					}});
			} catch (Exception e) {
				Log.i(MicroEmulator.LOG_TAG, fieldName+" "+defaultValue+" "+title+" "+type+" "+e.getMessage());
				//e.printStackTrace();
			}     
		}
	}

}
