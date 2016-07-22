package com.topher.brockman;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v17.leanback.app.PlaybackControlGlue;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.*;
import com.topher.brockman.api.TSchau;

import java.io.IOException;
import android.os.Handler;

/**
 * Created by t0phr_000 on 22.07.2016.
 */
public class PlaybackGlue extends PlaybackControlGlue{

    private static int[] seekSpeeds =  {PLAYBACK_SPEED_FAST_L0,
            PLAYBACK_SPEED_FAST_L1,
            PLAYBACK_SPEED_FAST_L2,
            PLAYBACK_SPEED_FAST_L3,
            PLAYBACK_SPEED_FAST_L4};

    private MediaPlayer mp;
    private Uri mediaUri;
    private boolean hasValidMedia = false;
    private TSchau video;
    private PlayerActivity activity;
    private int supportedActionsMask = ACTION_PLAY_PAUSE | ACTION_FAST_FORWARD | ACTION_REWIND;

    public PlaybackGlue(Context context, PlaybackOverlayFragment fragment) {
        super(context, fragment, seekSpeeds);
        mp = new MediaPlayer();
        activity = (PlayerActivity) fragment.getActivity();
        video = ((PlayerControlsFragment) fragment).getVideo();

        try {
            mp.setDataSource(video.getVideoUrl());
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setDisplay(activity.getVideoView().getHolder());
                    mp.start();
                    onStateChanged();
                    onMetadataChanged();
                }
            });

            hasValidMedia = true;
        } catch (IOException e) { // TODO: proper exception handling
            e.printStackTrace();
        }
    }

    @Override
    public PlaybackControlsRowPresenter createControlsRowAndPresenter() {
        return super.createControlsRowAndPresenter();
    }

    @Override
    public boolean hasValidMedia() {
        return hasValidMedia;
    }

    @Override
    public boolean isMediaPlaying() {
        return mp.isPlaying();
    }

    @Override
    public CharSequence getMediaTitle() {
        return video.getTitle();
    }

    @Override
    public CharSequence getMediaSubtitle() {
        return null;
    }

    @Override
    public int getMediaDuration() {
        return video.getDuration() * 1000;
    }

    @Override
    public Drawable getMediaArt() {
        return null;
    }

    @Override
    public long getSupportedActions() {
        return supportedActionsMask;
    }

    @Override
    public int getCurrentSpeedId() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mp.getCurrentPosition();
    }

    @Override
    protected void startPlayback(int speed) {
        mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
        mp.start();
        onStateChanged();
    }

    @Override
    protected void pausePlayback() {
        mp.pause();
        onStateChanged();
    }

    @Override
    protected void skipToNext() {
        return;
    }

    @Override
    protected void skipToPrevious() {
        return;
    }

    @Override
    protected void onRowChanged(PlaybackControlsRow row) {
        ArrayObjectAdapter a = ((PlayerControlsFragment) getFragment()).getRowsAdapter();
        a.notifyArrayItemRangeChanged(a.indexOf(row), 10);
    }

    @Override
    public void enableProgressUpdating(boolean enable) {
        if (!enable) return;

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                updateProgress();
                onStateChanged();
                onMetadataChanged();
                handler.postDelayed(this, getUpdatePeriod());
            }
        };

        handler.postDelayed(r, getUpdatePeriod());
    }
}
