package com.chenyou.localvideoplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.chenyou.localvideoplayer.R;

public class SplashActivity extends Activity {

    private Handler handler = new Handler();
    private boolean isStartMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //延时2秒进入主页面
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //在主线程执行
                startMainActivity();
            }
        }, 2000);
    }

    //进入主页面
    private void startMainActivity() {
        if (!isStartMain) {
            isStartMain = true;
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            //关闭启动页面
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有消息
        handler.removeCallbacksAndMessages(null);
    }
}
