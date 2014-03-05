package org.microemu.android;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	RelativeLayout panel;
	SharedPreferences prefs;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		panel = new RelativeLayout(this);
		setContentView(panel);
		LinearLayout localLinearLayout = new LinearLayout(this);
		localLinearLayout.setOrientation(1);
		panel.addView(localLinearLayout,new RelativeLayout.LayoutParams(-2, -2));
		TextView localTextView = new TextView(this);
		localTextView.setText("\u0420\u0430\u0437\u043C\u0435\u0440\u044B \u0448\u0440\u0438\u0444\u0442\u043E\u0432:");
		localLinearLayout.addView(localTextView);
		
		final EditText localEditText1 = new EditText(this);
		localEditText1.setText(String.valueOf(prefs.getInt("smallSize", 12)));
		localLinearLayout.addView(localEditText1);
		
		final EditText localEditText2 = new EditText(this);
		localEditText2.setText(String.valueOf(prefs.getInt("mediumSize", 16)));
		localLinearLayout.addView(localEditText2);
		
		final EditText localEditText3 = new EditText(this);
		localEditText3.setText(String.valueOf(prefs.getInt("largeSize", 20)));
		localLinearLayout.addView(localEditText3);
		
		Button localButton = new Button(this);
		localButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				SharedPreferences.Editor localEditor = prefs.edit();
				try {
					localEditor.putInt("smallSize",Integer.decode(localEditText1.getText().toString()).intValue());
					localEditor.putInt("mediumSize",Integer.decode(localEditText2.getText().toString()).intValue());
					localEditor.putInt("largeSize",Integer.decode(localEditText3.getText().toString()).intValue());
					localEditor.commit();
				} catch (Exception localException) {
				}
			}
		});
		localButton.setText("Save");
		localLinearLayout.addView(localButton);
	}
}
