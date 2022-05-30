/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.cicoco.placeholder;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;

import java.util.Locale;

public class MainActivity extends CordovaActivity {
    private boolean isLoading = false;


    private View mLoadView;

    private FrameLayout mContentView;

    private boolean isFirstEnter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 提前初始化
        initView();
        preload();

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        //解决H5全屏键盘弹出遮挡问题
        AndroidBug5497Workaround.assistActivity(this, appView);
    }

    private void initView() {
        appView = makeWebView();
        createViews();
        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());

        // Wire the hardware volume controls to control media if desired.
        String volumePref = preferences.getString("DefaultVolumeStream", "");
        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    protected void createViews() {

        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mContentView = new FrameLayout(this);
        mContentView.addView(appView.getView(), 0);
        setContentView(mContentView);

        if (preferences.contains("BackgroundColor")) {
            try {
                int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
                // Background of activity:
                appView.getView().setBackgroundColor(backgroundColor);
            } catch (NumberFormatException e) {
            }
        }

        appView.getView().requestFocusFromTouch();
    }

    private void preload() {
        isLoading = true;
        isFirstEnter = false;
        mLoadView = initSplashView();
        mContentView.addView(mLoadView);
    }

    /**
     * Webview加载页面消息监听
     *
     * @param id The message id
     */
    @Override
    public Object onMessage(String id, Object data) {
        if ("onPageFinished".equals(id)) {
            if (!isFirstEnter) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeSplashView();
                    }
                }, 1000);
            }
        }
        return super.onMessage(id, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void removeSplashView() {
        isLoading = false;
        if (null != mLoadView) {
            mContentView.removeView(mLoadView);
            mLoadView = null;
        }
    }

    private View initSplashView() {
        View img = new ImageView(this);
        int splashId = getSplashId();
        if (splashId != 0) {
            img.setBackgroundResource(splashId);
        }
        return img;
    }


    private int getSplashId() {
        int drawableId = 0;
        String splashResource = preferences.getString("SplashScreen", "screen");
        if (splashResource != null) {
            drawableId = getResources().getIdentifier(splashResource, "drawable", getClass().getPackage().getName());
            if (drawableId == 0) {
                drawableId = getResources().getIdentifier(splashResource, "drawable", getPackageName());
            }
        }
        return drawableId;
    }

    @Override
    public void onBackPressed() {
        if (isLoading) {
            return;
        }

        super.onBackPressed();
    }


    /**
     * 解决全屏模式下加载Webview时，输入法遮挡输入框
     * 5497来源于Google官方Issues/id=5497
     */
    public static class AndroidBug5497Workaround {

        public static void assistActivity(Activity activity, CordovaWebView webView) {
            new AndroidBug5497Workaround(activity, webView);
        }

        private View mChildOfContent;
        private int usableHeightPrevious;
        private FrameLayout.LayoutParams frameLayoutParams;

        private AndroidBug5497Workaround(Activity activity, CordovaWebView webView) {
            FrameLayout content = activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    //过滤网页条件
                    if (!TextUtils.isEmpty(webView.getUrl())) {
                        possiblyResizeChildOfContent();
                    }
                }
            });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                //当View Tree改变时直接设置为高度为当前可见视窗
                frameLayoutParams.height = usableHeightNow;
                mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        }

        private int computeUsableHeight() {
            // 全屏模式下： return r.bottom
            // 非全屏模式下：return r.bottom - r.top
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return r.bottom;
        }
    }

}
