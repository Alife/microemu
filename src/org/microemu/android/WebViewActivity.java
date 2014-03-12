package org.microemu.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.opera.mini.mod422.R;
 
public class WebViewActivity extends Activity{
	private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		webView = (WebView) findViewById(R.id.webView1);
		
		webView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
		webView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
		//webView.getSettings().setDefaultFontSize(5);
			
		String url = "http://google.com";
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			url = getIntent().getDataString();
		}
		webView.loadUrl(url);
		webView.setWebChromeClient(new chromeClient()); 
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);// 使用当前WebView处理跳转
				return true;//true表示此事件在此处被处理，不需要再广播
			}
			@Override	//转向错误时的处理
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				Toast.makeText(WebViewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
			}
		});
	}
	@Override	//默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class chromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// 动态在标题栏显示进度条
			WebViewActivity.this.setProgress(newProgress * 100);
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			// 设置当前activity的标题栏
			WebViewActivity.this.setTitle(title);
			super.onReceivedTitle(view, title);
		}
	}

 } 
