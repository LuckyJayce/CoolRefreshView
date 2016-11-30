package com.shizhefei.view.coolrefreshview.demo.activity.moreviews;

import android.os.Bundle;
import android.webkit.WebView;

import com.shizhefei.fragment.LazyFragment;
import com.shizhefei.view.coolrefreshview.CoolRefreshView;
import com.shizhefei.view.coolrefreshview.demo.R;
import com.shizhefei.view.coolrefreshview.demo.events.RefreshEvent;

/**
 * Created by LuckyJayce on 2016/11/29.
 */

public class WebViewFragment extends LazyFragment implements RefreshEvent {
    private CoolRefreshView coolRefreshView;
    private WebView webView;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_webview);
        coolRefreshView = (CoolRefreshView) findViewById(R.id.webview_funnyRefreshView);
        webView = (WebView) findViewById(R.id.webview_webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.baidu.com");
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        coolRefreshView.setRefreshing(refreshing);
    }

}
