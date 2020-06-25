package com.chenyou.localvideoplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenyou.localvideoplayer.R;
import com.chenyou.localvideoplayer.domian.MediaItem;
import com.chenyou.localvideoplayer.utils.Utils;
import java.util.ArrayList;

public class VideoPagerAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<MediaItem> mMediaItems;

    private Utils mUtils;

    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems, boolean isVideo) {
        mContext = context;
        mMediaItems = mediaItems;
        mUtils = new Utils();
    }

    @Override
    public int getCount() {
        return mMediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_video_pager, null);
            viewHolder = new ViewHolder();
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaItem mediaItem = mMediaItems.get(position);
        viewHolder.tv_name.setText(mediaItem.getName());
        viewHolder.tv_size.setText(Formatter.formatFileSize(mContext, mediaItem.getSize()));
        viewHolder.tv_duration.setText(mUtils.stringForTime((int) mediaItem.getDuration()));
            //显示视频的图片
            viewHolder.iv_icon.setImageResource(R.drawable.video_default_icon);
        return convertView;
    }

    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }
}
