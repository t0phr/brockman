package com.topher.brockman;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v17.leanback.app.PlaybackControlGlue;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.topher.brockman.api.Broadcast;

import java.io.IOException;

import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;

/**
 * Created by t0phr_000 on 22.07.2016.
 */
public class PlaybackGlue extends PlaybackControlGlue {
    private final static String TAG = "PlaybackGlue";
    private static final boolean DEBUG = false;

    private static int[] seekSpeeds =  {
            PLAYBACK_SPEED_FAST_L0,
            PLAYBACK_SPEED_FAST_L1,
            PLAYBACK_SPEED_FAST_L2,
            PLAYBACK_SPEED_FAST_L3,
            PLAYBACK_SPEED_FAST_L4
    };

    private MediaPlayer mp;
    private int playbackSpeed;
    private boolean hasValidMedia = false;
    private Broadcast video;
    private PlayerActivity activity;
    private int supportedActionsMask = ACTION_PLAY_PAUSE | ACTION_FAST_FORWARD | ACTION_REWIND;

    private Handler progressUpdateHandler;
    private boolean continueProgressUpdates = true;
    private ProgressUpdater progressUpdater;

    private Seeker seeker;
    private boolean seeking = false;
    private int seekOffset = 0;

    @Override
    protected void onRowChanged(PlaybackControlsRow row) {
        ArrayObjectAdapter adapter = ((PlayerControlsFragment) getFragment())
                .getRowsAdapter();
        if (adapter != null)
            adapter.notifyArrayItemRangeChanged(0,1);
    }

    public PlaybackGlue(Context context, final PlaybackOverlayFragment fragment) {
        super(context, fragment, seekSpeeds);
        if (DEBUG) Log.v(TAG, "PlaybackGlue");

        mp = new MediaPlayer();
        progressUpdateHandler = new Handler();
        progressUpdater = new ProgressUpdater();
        activity = (PlayerActivity) fragment.getActivity();
        video = ((PlayerControlsFragment) fragment).getVideo();

        try {
            mp.setDataSource(video.getVideoUrl());
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();
                    float videoProportion = (float) videoWidth / (float) videoHeight;

                    Point screenSize = new Point();
                    activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
                    float screenProportion = (float) screenSize.x / (float) screenSize.y;

                    android.view.ViewGroup.LayoutParams lp = activity.getVideoView().getLayoutParams();

                    if (videoProportion > screenProportion) {
                        lp.width = screenSize.x;
                        lp.height = (int) ((float) screenSize.x / videoProportion);
                    } else {
                        lp.width = (int) (videoProportion * (float) screenSize.y);
                        lp.height = screenSize.y;
                    }

                    activity.getVideoView().setLayoutParams(lp);

                    activity.getProgressBar().setVisibility(View.GONE);
                    mp.setDisplay(activity.getVideoView().getHolder());
                    startPlayback(PLAYBACK_SPEED_NORMAL);
                }
            });

            hasValidMedia = true;
        } catch (IOException e) {
            Log.e(TAG, "Video on url "
                    + video.getVideoUrl()
                    + " not found. Check network connextion.");
        }
    }

    @Override
    public PlaybackControlsRowPresenter createControlsRowAndPresenter() {
        if (DEBUG) Log.v(TAG, "createControlsRowAndPresenter");
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
        return playbackSpeed;
    }

    @Override
    public int getCurrentPosition() {
        if (seeking) {
            return mp.getCurrentPosition() + seekOffset;
        }

        return mp.getCurrentPosition();
    }

    @Override
    protected void startPlayback(int speed) {
        if (DEBUG) Log.v(TAG, "startPlayback");

        switch (Math.abs(speed)) {
            case PLAYBACK_SPEED_NORMAL:
                mp.getPlaybackParams().setSpeed(1.0f);
                playbackSpeed = speed;
                if (seeking) seeker.stop();
                if (!isMediaPlaying()) mp.start();
                setFadingEnabled(true);
                break;
            case PLAYBACK_SPEED_FAST_L0:
            case PLAYBACK_SPEED_FAST_L1:
            case PLAYBACK_SPEED_FAST_L2:
            case PLAYBACK_SPEED_FAST_L3:
            case PLAYBACK_SPEED_FAST_L4:
                int sign = (int) Math.signum(speed);
                if (seeker == null || seeker.isStopped())
                    seeker = new Seeker(sign * 8);
                else if (speed < PLAYBACK_SPEED_FAST_L4)
                    seeker.updateSpeed(seeker.getSpeed() * 2);
                playbackSpeed = speed;
                break;
        }

        onStateChanged();
        onMetadataChanged();
    }

    public void pauseView() {
        mp.pause();
    }

    @Override
    protected void pausePlayback() {
        if (DEBUG) Log.v(TAG, "pausePlayback");
        mp.pause();
        playbackSpeed = PLAYBACK_SPEED_PAUSED;
        setFadingEnabled(false);
        onStateChanged();
        onMetadataChanged();
    }

    @Override
    protected void skipToNext() {
        if (DEBUG) Log.v(TAG, "skipToNext");
        return;
    }

    @Override
    protected void skipToPrevious() {
        if (DEBUG) Log.v(TAG, "skipToPrevious");
        return;
    }

    @Override
    public void enableProgressUpdating(boolean enable) {
        if (DEBUG) Log.v(TAG, "enableProgressUpdating" + ", " + enable);

        if (!enable) {
            continueProgressUpdates = false;
        }
        else {
            continueProgressUpdates = true;
            progressUpdateHandler.postDelayed(progressUpdater, getUpdatePeriod());
        }
    }

    private void resumeProgressUpdate() {
        if (continueProgressUpdates)
            progressUpdateHandler.postDelayed(progressUpdater, getUpdatePeriod());
    }

    @Override
    public void onActionClicked(Action action) {
        super.onActionClicked(action);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (DEBUG) Log.v(TAG, "onKey" + ", " + keyCode);

        if (keyCode == 0) {
            return super.onKey(v, KEYCODE_MEDIA_PLAY_PAUSE, event);
        }
        return super.onKey(v, keyCode, event);
    }

    private class ProgressUpdater implements Runnable {
        private static final String TAG = "ProgressUpdater";

        @Override
        public void run() {
            if (DEBUG) Log.v(TAG, "run");

            updateProgress();
            resumeProgressUpdate();
        }
    }

    private class Seeker implements Runnable {
        public static final String TAG = "Seeker";
        private int interval;
        private int speed;
        private boolean stop = false;
        private Handler handler;
        private int stepSize;
        private boolean backwards;

        public void updateSpeed(int speed) {
            if (DEBUG) Log.v(TAG, "updateSpeed");

            this.backwards = speed > 0 ? false : true;
            this.speed = speed;
            this.stepSize = (int) Math.signum(speed) * 1000;
            this.interval = 1000 / Math.abs(this.speed);
        }

        public int getSpeed() {
            return speed;
        }

        public void stop() {
            if (DEBUG) Log.v(TAG, "stop");
            this.stop = true;
        }

        public boolean isStopped() {
            if (DEBUG) Log.v(TAG, "isStopped");
            return this.stop;
        }

        public Seeker(int speed) {
            if (DEBUG) Log.v(TAG, "Seeker");
            seeking = true;
            this.updateSpeed(speed);
            pausePlayback();
            handler = new Handler();
            handler.postDelayed(this, interval);
        }

        private void seek() {
            if (DEBUG) Log.v(TAG, "seek");
            mp.seekTo(mp.getCurrentPosition() + seekOffset);
            seeking = false;
            seekOffset = 0;
        }

        @Override
        public void run() {
            if (DEBUG) Log.v(TAG, "run");

            int nextSeekPoint = mp.getCurrentPosition() + seekOffset + stepSize;

            if ((backwards && 0 < nextSeekPoint)
                    || (!backwards && getMediaDuration() > nextSeekPoint)) {
                seekOffset += stepSize;
                updateProgress();
            }

            if (!stop)
                handler.postDelayed(this, interval);
            else seek();
        }
    }
}
