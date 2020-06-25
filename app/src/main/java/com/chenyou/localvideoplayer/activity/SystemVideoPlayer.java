package com.chenyou.localvideoplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chenyou.localvideoplayer.R;
import com.chenyou.localvideoplayer.domian.MediaItem;
import com.chenyou.localvideoplayer.utils.CacheUtils;
import com.chenyou.localvideoplayer.utils.Utils;
import com.chenyou.localvideoplayer.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SystemVideoPlayer extends Activity {

    /**
     * 进度更新
     */
    private static final int PROGRESS = 0;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 默认播放
     */
    private static final int DEFAULT_SCREEN = 3;
    /**
     * 全屏播放
     */
    private static final int FULL_SCREEN = 4;
    /**
     * 显示网速
     */
    private static final int SHOW_SPEED = 5;

    @BindView(R.id.videoview)
    VideoView videoview;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.iv_battery)
    ImageView ivBattery;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.btn_voice)
    Button btnVoice;
    @BindView(R.id.seekbar_voice)
    SeekBar seekbarVoice;
    @BindView(R.id.btn_switch_player)
    Button btnSwitchPlayer;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.tv_current_time)
    TextView tvCurrentTime;
    @BindView(R.id.seekbar_video)
    SeekBar seekbarVideo;
    @BindView(R.id.tv_duration)
    TextView tvDuration;
    @BindView(R.id.btn_video_exit)
    Button btnVideoExit;
    @BindView(R.id.btn_video_pre)
    Button btnVideoPre;
    @BindView(R.id.btn_video_start_pause)
    Button btnVideoStartPause;
    @BindView(R.id.btn_video_next)
    Button btnVideoNext;
    @BindView(R.id.btn_video_switch_screen)
    Button btnVideoSwitchScreen;
    @BindView(R.id.ll_bootom)
    LinearLayout llBootom;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.tv_loading_netspeed)
    TextView tvLoadingNetspeed;
    @BindView(R.id.tv_buffer_netspeed)
    TextView tvBufferNetspeed;
    private RelativeLayout rl_loading;
    private LinearLayout ll_buffer;

    /**
     * 视频管理器
     */
    private AudioManager am;
    /**
     * 当前的音量：0~15
     */
    private int currentVolume;
    /**
     * 最大音量
     */
    private int maxVolume;
    /**
     * 屏幕的宽
     */
    private int screenWidth;
    /**
     * 屏幕的高
     */
    private int screenHeight;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    private Uri uri;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private Utils mUtils;
    /**
     * 是否是网络资源
     */
    private boolean isNetUri = false;
    private int videoWidth;
    private int videoHeight;
    private boolean isFullScren = false;
    private BatteryReceiver receiver;
    /**
     * 手势识别器
     */
    private GestureDetector detector;

    private int prePosition = 0;

    private float startX;
    private float startY;
    private float touchRang;
    private int mVol;

    /**
     * 震动
     */
    private Vibrator vibrator;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED:
                    String netSpeed = mUtils.getNetSpeed(SystemVideoPlayer.this);
                    tvBufferNetspeed.setText("缓存中.." + netSpeed);
                    tvLoadingNetspeed.setText("正在玩命加载中..." + netSpeed);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 1000);
                    break;
                case HIDE_MEDIACONTROLLER:
                    //隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:
                    //得到当前的播放进度
                    int currentPosition = videoview.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);

                    tvCurrentTime.setText(mUtils.stringForTime(currentPosition));
                    tvTime.setText(getSystemTime());

                    //设置缓冲效果
                    if (isNetUri) {
                        int buffer = videoview.getBufferPercentage();//0~100;
                        int totalBuffer = seekbarVideo.getMax() * buffer;
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    int buffer = currentPosition - prePosition;

                    if (videoview.isPlaying()) {
                        if (buffer < 500) {
                            ll_buffer.setVisibility(View.VISIBLE);
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }


                    prePosition = currentPosition;
                    //每一秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    /**
     * 得到系统时间
     *
     * @return
     */
    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);
        ButterKnife.bind(this);
        rl_loading = (RelativeLayout) findViewById(R.id.rl_loading);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);

        initData();
        getData();
        setData();
        setListener();
        //设置控制面板
//        videoview.setMediaController(new MediaController(this));
    }

    private void initData() {
        //实例化AudioManager
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //得到屏幕的高和宽
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        mUtils = new Utils();
        //注册监听电量广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver, intentFilter);

        //2.实例化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isFullScren) {
                    setVideoType(DEFAULT_SCREEN);
                } else {
                    setVideoType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    //隐藏
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    //显示
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }


    private void getData() {
        uri = getIntent().getData();//得到一个地址：文件浏览器，浏览器，相册
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //接收列表的信息
            MediaItem mediaItem = mediaItems.get(position);
            //设置播放路径
            videoview.setVideoPath(mediaItem.getData());
            //设置视频名称
            tvName.setText(mediaItem.getName());
            isNetUri = mUtils.isNetUri(mediaItem.getData());
        } else if (uri != null) {
            videoview.setVideoURI(uri);
            tvName.setText(uri.toString());
            isNetUri = mUtils.isNetUri(uri.toString());
        }
        setButtonState();
        //设置不锁屏
        videoview.setKeepScreenOn(true);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 设置上一个和下一个按钮的状态
     */
    private void setButtonState() {
        //播放列表
        if (mediaItems != null && mediaItems.size() > 0) {
            if (position == 0) {//第一个视频
                btnVideoPre.setEnabled(false);
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            } else if (position == mediaItems.size() - 1) {//最后一个视频
                btnVideoNext.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            } else {
                btnVideoNext.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoPre.setEnabled(true);
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }
        } else if (uri != null) {
            btnVideoNext.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoPre.setEnabled(false);
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
        } else {
            Toast.makeText(this, "没有播放地址", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListener() {
        //当底层解码器准备好的时候，回调这个方法
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();

                //1.得到视频的总时长和SeeKBar.setMax();
                int duration = videoview.getDuration();
                seekbarVideo.setMax(duration);

                //设置总时长
                tvDuration.setText(mUtils.stringForTime(duration));
                //2.发消息更新
                handler.sendEmptyMessage(PROGRESS);
                //开始播放
                videoview.start();
                if (mediaItems != null && mediaItems.size() > 0) {
                    String key = mediaItems.get(position).getData();
                    int history = CacheUtils.getInt(SystemVideoPlayer.this, key);
                    if (history > 0) {
                        videoview.seekTo(history);
                    }
                } else if (uri != null) {
                    String key = uri.toString();
                    int history = CacheUtils.getInt(SystemVideoPlayer.this, key);
                    if (history > 0) {
                        videoview.seekTo(history);
                    }
                }

                hideMediaController();
                setVideoType(DEFAULT_SCREEN);

                //隐藏加载页面
                rl_loading.setVisibility(View.GONE);

            }
        });

        //当播放出错的时候回调这个方法
        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //                Toast.makeText(SystemVideoPlayer.this, "播放出错了", Toast.LENGTH_SHORT).show();
                //1.播放不支持的视频格式--跳转到万能播放器继续播放
                startVitamioPlayer();
                //2.播放网络视频的过程中-网络中断 - 重新播放
                //3.视频文件中间部分有缺损---把下载模块解决掉
                return true;
            }
        });

        //当播放完成的时候回调这个方法
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(SystemVideoPlayer.this, "播放完成", Toast.LENGTH_SHORT).show();
//                finish();
                setPlayNext();
            }
        });

        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
    }

    /**
     * 跳转到万能播放器
     */
    private void startVitamioPlayer() {
//        if (videoview != null) {
//            videoview.stopPlayback();
//        }
//        //传视频列表
//        Intent intent =new Intent(this,VitamioVideoPlayer.class);
//        if (mediaItems != null && mediaItems.size() > 0) {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("videolist", mediaItems);
//            intent.putExtras(bundle);
//            intent.putExtra("position", position);
//        } else if (uri != null) {
//            intent.setData(uri);
//        }
//        startActivity(intent);
//        finish();
    }

    @OnClick({R.id.btn_voice, R.id.btn_switch_player, R.id.btn_video_exit, R.id.btn_video_pre, R.id.btn_video_start_pause, R.id.btn_video_next, R.id.btn_video_switch_screen})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_voice:
                isMute = !isMute;
                //音量设置
                updateVolume(currentVolume);
                break;
            case R.id.btn_switch_player:
                //播放器切换
                showSwichPlayerDialog();
                break;
            case R.id.btn_video_exit:
                //退出
                finish();
                break;
            case R.id.btn_video_pre:
                //播放上一个
                setPlayPre();
                break;
            case R.id.btn_video_start_pause:
                //开始暂停
                startAndPause();
                break;
            case R.id.btn_video_next:
                //播放下一个
                setPlayNext();
                break;
            case R.id.btn_video_switch_screen:
                //全屏播放
                if (isFullScren) {
                    setVideoType(DEFAULT_SCREEN);
                } else {
                    setVideoType(FULL_SCREEN);
                }
                break;
        }
    }

    private void setVideoType(int type) {
        switch (type) {
            case FULL_SCREEN:
                //全屏
                videoview.setVideoSize(screenWidth, screenHeight);
                isFullScren = true;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                break;

            case DEFAULT_SCREEN:
                //默认

                //真实视频的高和宽
                int mVideoWidth = videoWidth;
                int mVideoHeight = this.videoHeight;

                //要播放视频的宽和高
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    if (mVideoWidth * height < width * mVideoHeight) {
                        width = height * mVideoWidth / mVideoHeight;
                    } else if (mVideoWidth * height > width * mVideoHeight) {
                        height = width * mVideoHeight / mVideoWidth;
                    }
                    videoview.setVideoSize(width, height);
                }
                isFullScren = false;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                break;
        }
    }

    private void setPlayNext() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position >= 0) {
                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址
                tvName.setText(mediaItem.getName());
                isNetUri = mUtils.isNetUri(mediaItem.getData());
                setButtonState();

                if (position == mediaItems.size() - 1) {
                    Toast.makeText(SystemVideoPlayer.this, "已经是最后一个视频了", Toast.LENGTH_SHORT).show();
                }
                rl_loading.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        } else if (uri != null) {
            //退出播放器
            finish();
        }
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            //暂停
            videoview.pause();
            //按钮设置播放状态
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_play_selector);
        } else {
            //播放
            videoview.start();
            //按钮设置暂停状态
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    private void setPlayPre() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //上一个
            position--;
            if (position >= 0) {
                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址
                tvName.setText(mediaItem.getName());
                isNetUri = mUtils.isNetUri(mediaItem.getData());
                setButtonState();
                rl_loading.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前使用系统播放器播放，是否切换到万能播放器播放视频！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mediaItems != null && mediaItems.size() > 0) {
                    MediaItem mediaItem = mediaItems.get(position);
                    CacheUtils.putInt(SystemVideoPlayer.this,mediaItem.getData(),videoview.getCurrentPosition());
                } else if (uri != null) {
                    //uri本质上就是一个网络视频播放地址，uri.toString()就是把uri转化为字符串
                    CacheUtils.putInt(SystemVideoPlayer.this,uri.toString(),videoview.getCurrentPosition());
                }
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    /**
     * 根据传入的值修改音量
     *
     * @param volume
     */
    private void updateVolume(int volume) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);//设置音量seekBar进度
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            seekbarVoice.setProgress(volume);
            currentVolume = volume;
        }
    }

    class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//电量：0-100
            //主线程
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * 是否隐藏控制面板
     * true:显示
     * false:隐藏
     */
    private boolean isShowMediaController = false;

    private void hideMediaController() {
        llBootom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isShowMediaController = false;
    }

    private void showMediaController() {
        llBootom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当进度跟新的时候回调这个方法
         *
         * @param seekBar
         * @param progress 当前进度
         * @param fromUser 是否是由用户引起
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }
        }

        /**
         * 当手触碰SeekBar的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开SeeKbar的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                updateVolumeProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
    }

    private void updateVolumeProgress(int volume) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        seekbarVoice.setProgress(volume);
        currentVolume = volume;
        if (volume <= 0) {
            isMute = true;
        } else {
            isMute = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3.把事件给手势识别器解析
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //1.按下的时候记录初始值
                startX = event.getX();
                startY = event.getY();
                touchRang = Math.min(screenHeight, screenWidth);
                mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                //2.来到新的坐标
                float endX = event.getX();
                float endY = event.getY();

                if (endX < screenWidth / 2) {
                    //左边-滑动改变屏幕的亮度
                    System.out.println("左边-滑动改变屏幕的亮度");
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (startY - endY > FLING_MIN_DISTANCE && Math.abs(endY - startY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (startY - endY < FLING_MIN_DISTANCE && Math.abs(endY - startY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                } else {
                    //-右边-滑动改变声音
                    System.out.println("右边-滑动改变声音");
                    //3.计算偏移量
                    float distanceY = startY - endY;
                    //屏幕滑动的距离： 总距离 = 改变的声音： 最大音量
                    float changVolume = (distanceY / touchRang) * maxVolume;
                    //最终的声音= 原来的音量 + 改变的声音；
                    float volume = Math.min(Math.max(mVol + changVolume, 0), maxVolume);
                    if (changVolume != 0) {
                        updateVolumeProgress((int) volume);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     *
     * @param brightness
     */
    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200};
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200};
            vibrator.vibrate(pattern, -1);
        }
        Log.e("screenBrightness", "lp.screenBrightness= " + lp.screenBrightness);
        getWindow().setAttributes(lp);
    }

    /**
     * 按音量键改变音量
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume--;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
        return super.onKeyDown(keyCode, event);
    }

}
