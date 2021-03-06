/*
 * ============================================================================
 * Copyright (C) 2017 Kaltura Inc.
 *
 * Licensed under the AGPLv3 license, unless a different license for a
 * particular library is specified in the applicable library path.
 *
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.html
 * ============================================================================
 */

package com.kaltura.playkit.ads;

import android.content.Context;

import com.kaltura.playkit.PKController;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlayerEngineWrapper;
import com.kaltura.playkit.player.PKMediaSourceConfig;
import com.kaltura.playkit.plugins.ads.AdsProvider;

/**
 * @hide
 */

public class AdsPlayerEngineWrapper extends PlayerEngineWrapper implements PKAdProviderListener {

    private static final PKLog log = PKLog.get("AdsPlayerEngineWrapper");

    private Context context;
    private AdsProvider adsProvider;
    private PKMediaSourceConfig mediaSourceConfig;
    private DefaultAdControllerImpl defaultAdController;

    public AdsPlayerEngineWrapper(final Context context, AdsProvider adsProvider) {
        this.context = context;
        this.adsProvider = adsProvider;
        this.defaultAdController = new DefaultAdControllerImpl(adsProvider);
    }

    @Override
    public void load(PKMediaSourceConfig mediaSourceConfig) {
        this.mediaSourceConfig = mediaSourceConfig;
        if (adsProvider != null) {
            //incase no ads provided - need to prepare so treating load state as ad was requested
            if (adsProvider.getCuePoints() != null && adsProvider.getCuePoints().getAdCuePoints() != null && adsProvider.getCuePoints().getAdCuePoints().size() == 0) {
                adsProvider.setAdRequested(true); // need to prepare immeidatly
            }

            if (preparePlayerForPlayback()) {
                log.d("AdWrapper calling super.prepare");
                super.load(mediaSourceConfig);
            } else {
                log.d("AdWrapper setAdProviderListener");
                adsProvider.setAdProviderListener(this);
            }
        }
    }

    private boolean preparePlayerForPlayback() {

        return (adsProvider.isAdRequested() && (adsProvider.getCuePoints() == null || adsProvider.getAdInfo() == null)) || adsProvider.isAllAdsCompleted() ||
                adsProvider.isAdError() || adsProvider.isAdDisplayed() ||
                adsProvider.isAdRequested() && adsProvider.getCuePoints() != null && (!adsProvider.getCuePoints().hasPreRoll() || getCurrentPosition() > 0) ||
                adsProvider.getPlaybackStartPosition() != null && adsProvider.getPlaybackStartPosition() > 0 && !adsProvider.isAlwaysStartWithPreroll();
    }

    @Override
    public void play() {
        log.d("AdWrapper PLAY");
        if (adsProvider != null) {
            if (!adsProvider.isAdError()) {
                log.d("AdWrapper PLAY isAdDisplayed = " + adsProvider.isAdDisplayed() + " isAdPaused = " + adsProvider.isAdPaused() + " isAllAdsCompleted = " + adsProvider.isAllAdsCompleted());
                if (!adsProvider.isAllAdsCompleted()) {
                    if (!adsProvider.isAdRequested()) {
                        adsProvider.start();
                        return;
                    } else if (adsProvider.isAdDisplayed()) {
                        adsProvider.resume();
                        return;
                    }
                }
            }
            if (adsProvider.isAdDisplayed()) {
                return;
            }
        }

        log.d("AdWrapper decorator Calling player play");
        getView().showVideoSurface();
        super.play();
    }

    @Override
    public void pause() {
        if (adsProvider != null) {
            boolean isAdDisplayed = adsProvider.isAdDisplayed();
            log.d("AdWrapper PAUSE decorator isAdDisplayed = " + isAdDisplayed + " isAdPaused = " + adsProvider.isAdPaused() + " isAllAdsCompleted " + adsProvider.isAllAdsCompleted());
            if (isAdDisplayed && !adsProvider.isAdError()) {
                adsProvider.pause();
                return;
            }
        }

        if (super.isPlaying()) {
            log.d("AdWrapper decorator Calling content player pause");
            super.pause();
        }
    }

    @Override
    public long getCurrentPosition() {
        return super.getCurrentPosition();
    }

    @Override
    public long getProgramStartTime() {
        return super.getProgramStartTime();
    }

    @Override
    public long getDuration() {
        return super.getDuration();
    }

    @Override
    public void seekTo(long position) {
        log.d("AdWrapper seekTo");
        super.seekTo(position);
    }

    @Override
    public boolean isPlaying() {
        log.d("AdWrapper isPlaying");
        return super.isPlaying();
    }

    @Override
    public void setAnalyticsListener(AnalyticsListener analyticsListener) {
        super.setAnalyticsListener(analyticsListener);
    }

    @Override
    public void stop() {
        log.d("AdWrapper stop");
        if (adsProvider != null) {
            adsProvider.setAdRequested(false);
            adsProvider.destroyAdsManager();
        }
        super.stop();
    }

    @Override
    public <T extends PKController> T getController(Class<T> type) {
        if (type == AdController.class && defaultAdController != null) {
            return (T) this.defaultAdController;
        }
        return super.getController(type);
    }

    @Override
    public void onAdLoadingFinished() {
        log.d("onAdLoadingFinished pkPrepareReason");
        if (mediaSourceConfig == null) {
            log.e("AdWrapper onAdLoadingFinished mediaSourceConfig == null");
            return;
        }
        load(mediaSourceConfig);
        if (adsProvider != null) {
            adsProvider.removeAdProviderListener();
        }
    }
}
