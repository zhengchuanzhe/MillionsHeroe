package com.linda.answerhelper.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.linda.answerhelper.event.BitmapEvent;
import com.linda.answerhelper.event.StartCaptureEvent;
import com.linda.answerhelper.model.ReasultModel;
import com.linda.answerhelper.ocr.BaiduOcrImpl;
import com.linda.answerhelper.ocr.OcrIReader;
import com.linda.answerhelper.ocr.ServiceListener;
import com.linda.answerhelper.ocr.TessOCR;
import com.linda.answerhelper.util.Util;
import com.linda.tool.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by dahaolin on 2018/1/12.
 */

public class AnswerService extends Service {


    private static final int MESSAGE_RECOGNISE_RESULT = 101;
    private static final String KEY_RESULT = "result";
    private View mWebViewContainer;
    private WindowManager.LayoutParams mWebViewContainerParams;
    private Handler mHandler;
    private boolean isShowWebView;
    private WindowManager mWindowManager;
    private TextView mytextView;
    private final static String ABSOLUTE_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath();
    private OcrIReader myOcr;

    public class WebViewHandler extends Handler {

        WeakReference<WebView> webViewWeakReference;

        WebViewHandler(WebView webView) {
            webViewWeakReference = new WeakReference<WebView>(webView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_RECOGNISE_RESULT:
                    Bundle bundle = msg.getData();
                    String result = bundle.getString(KEY_RESULT);
                    mytextView.setText(result);
//                    if (!TextUtils.isEmpty(result) && webViewWeakReference.get() != null) {
//                        if (result.contains(".")){
//                            int index = result.indexOf(".");
//                            result = result.substring(index+1);
//                        }
//
//                       // webViewWeakReference.get().loadUrl("https://wap.sogou.com/web/searchList.jsp?keyword=" + result);
//                    }
                    break;
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        myOcr=new BaiduOcrImpl(getApplicationContext());
        initWebViewContainerParams();
        initToucher();
        mytextView=(TextView)mWebViewContainer.findViewById(R.id.textView);
    }

    private void initWebViewContainerParams() {
        mWebViewContainerParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWebViewContainerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWebViewContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mWebViewContainerParams.format = PixelFormat.RGBA_8888;
        mWebViewContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWebViewContainerParams.gravity = Gravity.START | Gravity.BOTTOM;
        mWebViewContainerParams.x = 0;
        mWebViewContainerParams.y = 0;
        mWebViewContainerParams.width = Util.getScreenSize(this)[0];
        mWebViewContainerParams.height = 400;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        mWebViewContainer = inflater.inflate(R.layout.toucher_layout, null);

        ImageButton mBtnRecognition = mWebViewContainer.findViewById(R.id.over_layer_button);
        mBtnRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new StartCaptureEvent());
            }
        });

        WebView mWebView = mWebViewContainer.findViewById(R.id.over_layer_webview);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setWebChromeClient(new WebChromeClient());

        mHandler = new WebViewHandler(mWebView);
    }

    private void initToucher() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.BOTTOM;
        params.x = 0;
        params.y = 400;
        params.width = 100;
        params.height = 100;

        View toucherLayout = new ImageButton(this);
        toucherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowWebView) {
                    dismiss();
                } else {
                    show();
                }
            }
        });
        mWindowManager.addView(toucherLayout, params);

    }

    private void show() {
        isShowWebView = true;
        mWindowManager.addView(mWebViewContainer, mWebViewContainerParams);
    }

    private void dismiss() {
        isShowWebView = false;
        mWindowManager.removeViewImmediate(mWebViewContainer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onGetBitmapEvent(BitmapEvent event) {
        recognise(event.getBitmap());
    }

    private void recognise(final Bitmap bitmap) {
        new Thread() {
            @Override
            public void run() {
                if (bitmap != null) {
                    myOcr.getOcr(bitmap,
                    new ServiceListener() {
                        @Override
                        public void onResult(ReasultModel result) {
                           // Toast.makeText(AnswerService.this, "结果:"+result, Toast.LENGTH_SHORT).show();
                            String ss= new httpSearch().searchResult(result);
                            System.out.println("结果:"+ss);

                            Message message = new Message();
                            message.what = MESSAGE_RECOGNISE_RESULT;
                            Bundle bundle = new Bundle();
                            bundle.putString(KEY_RESULT, ss);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                            bitmap.recycle();
                        }
                    });
                }
            }
        }.start();
    }

}
