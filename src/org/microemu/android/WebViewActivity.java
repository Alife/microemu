package org.microemu.android;

import com.opera.mini.mod422.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class WebViewActivity extends PreferenceActivity {
	RelativeLayout panel;
	SharedPreferences prefs;
	PreferenceScreen preferenceScreen;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			String dataString = getIntent().getDataString();
	
			final LayoutParams params = new WindowManager.LayoutParams();
			final RelativeLayout panel = new RelativeLayout(this);
			setContentView(panel);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			panel.setLayoutParams(lp);
			LinearLayout localLinearLayout = new LinearLayout(this);
			panel.addView(localLinearLayout);
			ImageView close = new ImageView(this);
			close.setImageResource(R.drawable.close);
			close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			
			final WebView webView = new WebView(this);
			webView.getSettings().setJavaScriptEnabled(false);// 设置使用够执行JS脚本
			webView.getSettings().setBuiltInZoomControls(true);// 设置使支持缩放
			// webView.getSettings().setDefaultFontSize(5);
			webView.loadUrl(dataString);
			//此方法可以处理webview 在加载时和加载完成时一些操作
			webView.setWebViewClient(new WebViewClient(){
			//重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
				@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {  
						view.loadUrl(url);
					return true;
				}
			});
			localLinearLayout.addView(webView);
			
			lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);//与父容器的左侧对齐
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);//与父容器的上侧对齐
//			lp.leftMargin=30;
//			lp.topMargin=30;
			close.setId(1);//设置这个View 的id 
			close.setLayoutParams(lp);//设置布局参数
	
			webView.addView(close);
		}

	}
	
}
