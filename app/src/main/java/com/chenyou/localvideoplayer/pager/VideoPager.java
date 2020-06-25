package com.chenyou.localvideoplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chenyou.localvideoplayer.R;
import com.chenyou.localvideoplayer.activity.SystemVideoPlayer;
import com.chenyou.localvideoplayer.adapter.VideoPagerAdapter;
import com.chenyou.localvideoplayer.domian.MediaItem;
import com.chenyou.localvideoplayer.utils.Utils;

import java.util.ArrayList;

/**
 * 本地视频
 */
public class VideoPager extends BasePager {

    private ListView lvVideoPager;
    private TextView tvNomedia;
    private ProgressBar pbLoading;

    private ArrayList<MediaItem> mMediaItems;

    private Utils mUtils;

    public VideoPager(Context context) {
        super(context);
        mUtils = new Utils();
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.video_pager, null);
        lvVideoPager = (ListView) view.findViewById(R.id.lv_video_pager);
        tvNomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
        lvVideoPager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //传视频列表
                Intent intent = new Intent(mContext, SystemVideoPlayer.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist", mMediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        System.out.println("本地视频数据初始化了。。。");
        getData();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //主线程
            if (mMediaItems != null && mMediaItems.size() > 0) {
                tvNomedia.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE);
                //设置适配器
                lvVideoPager.setAdapter(new VideoPagerAdapter(mContext, mMediaItems, true));
            } else {
                tvNomedia.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 获取数据
     */
    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                mMediaItems = new ArrayList<MediaItem>();
                ContentResolver contentResolver = mContext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Video.Media.DISPLAY_NAME,//在Sdcard显示的名称
                        MediaStore.Video.Media.DURATION,//视频的长度
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA//视频的绝对地址
                };
                Cursor cursor = contentResolver.query(uri, objects, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        String name = cursor.getString(0);
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        //把视频添加到列表中
                        mMediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }
}
