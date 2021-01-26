package com.huimv.szmc.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.huimv.android.basic.base.BaseFragment;
import com.huimv.szmc.R;
import com.huimv.szmc.activity.TestScanActivity;
import com.huimv.szmc.app.HaifmPApplication;
import com.huimv.szmc.constant.XtAppConstant;
import com.huimv.szmc.messageEvent.MessageEvent;
import com.huimv.szmc.util.SharePreferenceUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.app.Activity.RESULT_OK;

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
        //initWebView("http://hzsheep.ifarmcloud.com/hyAppbutcher/#/addSheep?item=");

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
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        settings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(false);
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        // 支持缩放
        settings.setSupportZoom(true);

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
        getDataFromWeb();
    }
    private void getDataFromWeb() {
        mainWebView.registerHandler("scanQrCode", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e("TAG", "js返回：" + data);
                //Android返回给JS的消息
                function.onCallBack("我是js调用Android返回数据：" + data);
                jumpToScan();
            }
        });
    }


    private void jumpToScan() {
        //startActivity(new Intent(getActivity(), TestScanActivity.class));
        startActivityForResult(new Intent(getActivity(), TestScanActivity.class), 0x99);
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
    private void sendDataQrCodeToWeb(String qrcode) {
        Log.d("sendDataToWeb", "sendDataToWeb: " + qrcode);
        mainWebView.callHandler("scanResult", qrcode, new CallBackFunction() {
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

        if (event.getId() == 3) {
            Log.d(TAG, "onMessageEvent: " + event.getData());

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b= data.getExtras(); //data为B中回传的Intent
                String str= b.getString("result");//str即为回传的值
                sendDataQrCodeToWeb(str);
                break;
            default:
                break;
        }
    }
}
