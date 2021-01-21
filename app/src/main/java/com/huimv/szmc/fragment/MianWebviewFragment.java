package com.huimv.szmc.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.huimv.android.basic.base.BaseFragment;
import com.huimv.szmc.R;
import com.huimv.szmc.app.HaifmPApplication;
import com.huimv.szmc.constant.XtAppConstant;
import com.huimv.szmc.messageEvent.MessageEvent;
import com.huimv.szmc.util.SharePreferenceUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MianWebviewFragment extends BaseFragment{
    private final static String TAG = "MianWebviewFragment";
    public BridgeWebView mainWebView;
    private ProgressBar mProgressBar;
    private String url = "http://" + XtAppConstant.SERVICE_IP + "/szmc";
    private SharePreferenceUtil mSpUtil;

    @Override
    public View onInitView(LayoutInflater inflater, ViewGroup rootView, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.webview_fragment, null);
        mainWebView = (BridgeWebView) view.findViewById(R.id.mainWebView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgressBar);
        mProgressBar.setMax(100);
        mSpUtil = HaifmPApplication.getInstance().getSpUtil();
        String type = XtAppConstant.type2;
        String accoutParma = "/login.htm";
        initWebView("http://122.112.212.35/hyAppbutcher/#/");
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        String isAPPStrUrl = "";
        isAPPStrUrl = url;
        Logger.d("URL", isAPPStrUrl);
        mainWebView.getSettings().setJavaScriptEnabled(true);
        mainWebView.getSettings().setDomStorageEnabled(true);
        mainWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getActivity().getCacheDir().getAbsolutePath();
        mainWebView.getSettings().setAppCachePath(appCachePath);
        mainWebView.getSettings().setAllowFileAccess(true);
        mainWebView.getSettings().setAppCacheEnabled(true);

        WebSettings settings = mainWebView.getSettings();
        settings.setBuiltInZoomControls(false); // 显示放大缩小 controler
        settings.setSupportZoom(false); // 可以缩放
        settings.setDefaultZoom(ZoomDensity.CLOSE);// 默认缩放模式
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);


        // mainWebView.setWebViewClient(new MyWebViewClient(mainWebView));
        //设置Web视图
        mainWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
                if (newProgress == 5) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        //mainWebView.setWebViewClient(new mWebViewClient ());
        mainWebView.loadUrl(isAPPStrUrl);
        //mainWebView.loadUrl("http://www.ifarmcloud.com/ifm/login.htm?type=4");

    }

    /**
     * 自定义的WebViewClient
     */
    class MyWebViewClient extends BridgeWebViewClient {

        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }
    }

    private void sendDataToWeb(String rfidTemp) {
        Log.d("sendDataToWeb", "sendDataToWeb: " + rfidTemp);
        mainWebView.callHandler("functionRfidTemp", rfidTemp, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Logger.d("Recdata", "收到数据");
            }
        });
    }

    //Web视图
    private class mWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event) {
        sendDataToWeb(event);
    };

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(MessageEvent event) {
        if (event.getId() == 1) {
            Log.d(TAG, "onMessageEvent: " + event.getData());
            initWebView(event.getData());
        }
        if (event.getId() == 2) {
            initWebView(url + "/logout.htm?sbid=" +  mSpUtil.getUniqueID());
        }
    };
}
