package com.chenyou.localvideoplayer.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.chenyou.localvideoplayer.R;
import com.chenyou.localvideoplayer.pager.BasePager;
import com.chenyou.localvideoplayer.pager.VideoPager;
import com.chenyou.localvideoplayer.view.ReplaceFragment;


/**
 * 主页面
 */
public class MainActivity extends FragmentActivity {

    private VideoPager videoPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPager = new VideoPager(this);
        setFragment();

    }


    private void setFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();//开启事务
        ft.replace(R.id.fl_main, new ReplaceFragment(getBasePager()));
        ft.commit();
    }

    private BasePager getBasePager() {
        BasePager basePager = videoPager;
        if (basePager != null && !basePager.isInitData) {
            basePager.isInitData = true;
            basePager.initData();
        }
        return basePager;
    }

    private long startTime;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - startTime > 2000) {
            startTime = System.currentTimeMillis();
            Toast.makeText(MainActivity.this, "再点一次退出", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}
