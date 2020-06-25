package com.chenyou.localvideoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 作用：自定义VideoView
 */
public class VideoView extends android.widget.VideoView {

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);//保存测量结果
    }

    /**
     * 设置视频的画面大小
     *
     * @param width  要设置视频的宽
     * @param height 要设置视频的高
     */
    public void setVideoSize(int width, int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }
}
