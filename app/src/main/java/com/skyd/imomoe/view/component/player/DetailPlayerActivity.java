package com.skyd.imomoe.view.component.player;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.skyd.skin.core.SkinBaseActivity;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationOption;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE;

/**
 * 详情模式播放页面基础类
 */
public abstract class DetailPlayerActivity<T extends GSYBaseVideoPlayer> extends SkinBaseActivity implements VideoAllCallBack {

    protected boolean isPlay;

    // 是否是在onPause方法里自动暂停的
    protected boolean isPause;

    protected OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 选择普通模式
     */
    public void initVideo() {
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, getGSYVideoPlayer(), getOrientationOption());
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        if (getGSYVideoPlayer().getFullscreenButton() != null) {
            getGSYVideoPlayer().getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFull();
                    clickForFullScreen();
                }
            });
        }
    }

    /**
     * 选择builder模式
     */
    public void initVideoBuilderMode() {
        initVideo();
        getGSYVideoOptionBuilder().
                setVideoAllCallBack(this)
                .build(getGSYVideoPlayer());
    }

    public void showFull() {
        if (orientationUtils.getIsLand() != 1) {
            //直接横屏
            orientationUtils.resolveByClick();
        }
        //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusBar
        getGSYVideoPlayer().startWindowFullscreen(DetailPlayerActivity.this, hideActionBarWhenFull(), hideStatusBarWhenFull());

    }

    @Override
    public void onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (getGSYVideoPlayer().getCurrentPlayer().getCurrentState() != CURRENT_STATE_PAUSE) {
            getGSYVideoPlayer().getCurrentPlayer().onVideoPause();
            if (orientationUtils != null) {
                orientationUtils.setIsPause(true);
            }
            isPause = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            getGSYVideoPlayer().getCurrentPlayer().onVideoResume();
            if (orientationUtils != null) {
                orientationUtils.setIsPause(false);
            }
            isPause = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getGSYVideoPlayer().getCurrentPlayer().release();
        }
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            getGSYVideoPlayer().onConfigurationChanged(this, newConfig, orientationUtils, hideActionBarWhenFull(), hideStatusBarWhenFull());
        }
    }

    @Override
    public void onStartPrepared(String url, Object... objects) {

    }

    @Override
    public void onPrepared(String url, Object... objects) {

        if (orientationUtils == null) {
            throw new NullPointerException("initVideo() or initVideoBuilderMode() first");
        }
        //开始播放了才能旋转和全屏
        orientationUtils.setEnable(getDetailOrientationRotateAuto() && !isAutoFullWithSize());
        isPlay = true;
        isPause = false;
    }

    @Override
    public void onClickStartIcon(String url, Object... objects) {

    }

    @Override
    public void onClickStartError(String url, Object... objects) {

    }

    @Override
    public void onClickStop(String url, Object... objects) {

    }

    @Override
    public void onClickStopFullscreen(String url, Object... objects) {

    }

    @Override
    public void onClickResume(String url, Object... objects) {

    }

    @Override
    public void onClickResumeFullscreen(String url, Object... objects) {

    }

    @Override
    public void onClickSeekbar(String url, Object... objects) {

    }

    @Override
    public void onClickSeekbarFullscreen(String url, Object... objects) {

    }

    @Override
    public void onAutoComplete(String url, Object... objects) {

    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {

    }

    @Override
    public void onQuitFullscreen(String url, Object... objects) {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
    }

    @Override
    public void onQuitSmallWidget(String url, Object... objects) {

    }

    @Override
    public void onEnterSmallWidget(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekVolume(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekPosition(String url, Object... objects) {

    }

    @Override
    public void onTouchScreenSeekLight(String url, Object... objects) {

    }

    @Override
    public void onPlayError(String url, Object... objects) {

    }

    @Override
    public void onClickStartThumb(String url, Object... objects) {

    }

    @Override
    public void onClickBlank(String url, Object... objects) {

    }

    @Override
    public void onClickBlankFullscreen(String url, Object... objects) {

    }

    @Override
    public void onComplete(String url, Object... objects) {

    }

    public boolean hideActionBarWhenFull() {
        return true;
    }

    public boolean hideStatusBarWhenFull() {
        return true;
    }

    /**
     * 可配置旋转 OrientationUtils
     */
    public OrientationOption getOrientationOption() {
        return null;
    }

    /**
     * 播放控件
     */
    public abstract T getGSYVideoPlayer();

    /**
     * 配置播放器
     */
    public abstract GSYVideoOptionBuilder getGSYVideoOptionBuilder();

    /**
     * 点击了全屏
     */
    public abstract void clickForFullScreen();

    /**
     * 是否启动旋转横屏，true表示启动
     */
    public abstract boolean getDetailOrientationRotateAuto();

    /**
     * 是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，注意，这时候默认旋转无效
     */
    public boolean isAutoFullWithSize() {
        return false;
    }
}
