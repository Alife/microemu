package org.android.view;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.android.util.Tools;
import org.microemu.opm422.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

// 主要实现在ListPreference弹出的对话框中的某一项选中时，再弹出一个编辑框	http://wx1985113.iteye.com/blog/1326575
/**
 * @author wu xiang
 * @date Dec 26, 2011
 * @fileName TestPre.java
 *
 */
public class EditListPreference extends ListPreference implements OnPreferenceClickListener {
	
	private Toast myToast;
	public int inputType;
	String customValue="Custom Value";
	/**
	 * @param context
	 * @param attrs
	 */
	public EditListPreference(Context context) {
		super(context);
		customValue = context.getText(R.string.editListPreference_customValue).toString();
		myToast=Toast.makeText(context, "", Toast.LENGTH_SHORT);
	}
	public EditListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		customValue = context.getText(R.string.editListPreference_customValue).toString();
		myToast=Toast.makeText(context, "", Toast.LENGTH_SHORT);
	}

	@Override
	public CharSequence[] getEntries() {
		CharSequence[] entries = super.getEntries();
		if(Arrays.binarySearch(entries, customValue)<-1){
			entries=Tools.add(entries,customValue);
		}
		return entries;
	}
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		int checkItem=getValueIndex(getValue());
		builder.setSingleChoiceItems(this.getEntries(), checkItem, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				if(getEntries()[which].equals(customValue)){
					final EditText et=new EditText(EditListPreference.this.getContext());
					et.setText(getValue());
					et.setInputType(inputType);
					new AlertDialog.Builder(EditListPreference.this.getContext())
					.setTitle(customValue).setView(et)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							String sValue=et.getText().toString();
							if(Pattern.compile("\\d+").matcher(sValue).matches()){
								int iValue=Integer.parseInt(sValue);
								if(iValue>0&&iValue<50){
									setValue(iValue+"");
								}
							}else{
								myToast.setText(R.string.editListPreference_customValue_formatError);
								myToast.show();
							}
						}
					})
					.setNeutralButton(android.R.string.cancel, null)
					.create().show();
				}else{
					setValue(getEntryValues()[which]+"");
				}
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(null, null);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		this.getOnPreferenceChangeListener().onPreferenceChange(this, this.getValue());		
	}

	@Override
	public void setOnPreferenceChangeListener(
			OnPreferenceChangeListener onPreferenceChangeListener) {
		// TODO Auto-generated method stub
		super.setOnPreferenceChangeListener(onPreferenceChangeListener);
	}
	private int getValueIndex(String value){
		int len=0;
		for(int i=getEntryValues().length-1;i>=len;i--){
			if(value.equals(getEntryValues()[i])){
				return i+1;
			}
		}
		return len;//选中“自定义” 一项
	}
}
