package com.chenyou.localvideoplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 作用：缓存工具
 */
public class CacheUtils {
    public static void putInt(Context context, String key, int values) {
    }


    /**
     * 得到播放记录
     *
     * @return
     */
    public static int getInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("atguigu", Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }
}
