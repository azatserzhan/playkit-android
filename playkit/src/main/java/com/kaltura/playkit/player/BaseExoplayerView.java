package com.kaltura.playkit.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.kaltura.android.exoplayer2.SimpleExoPlayer;
import com.kaltura.android.exoplayer2.ui.SubtitleView;

public abstract class BaseExoplayerView extends PlayerView {

    public BaseExoplayerView(Context context) {
        super(context);
    }

    public BaseExoplayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseExoplayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void setPlayer(SimpleExoPlayer player, boolean useTextureView, boolean isSurfaceSecured, boolean hideVideoViews);

    public abstract void setVideoSurfaceProperties(boolean useTextureView, boolean isSurfaceSecured, boolean hideVideoViews);

    public abstract SubtitleView getSubtitleView();

    public abstract void setVideoSurfaceView(SurfaceView surfaceView);

}
