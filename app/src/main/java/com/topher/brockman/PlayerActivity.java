package com.topher.brockman;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.VideoView;
import com.topher.brockman.api.TSchau;

/**
 * Created by topher on 21/07/16.
 */
public class PlayerActivity extends Activity
        implements PlayerControlsFragment.PlayerControlsListener {
    private VideoView mVideoView;
    private TSchau mVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mVideoView = (VideoView) findViewById( R.id.video_view );
        mVideo = (TSchau) getIntent().getSerializableExtra(MainFragment.
                EXTRA_VIDEO);
    }

    public VideoView getVideoView() {
        return mVideoView;
    }

    public TSchau getVideo() {
        return mVideo;
    }

    @Override
    public void play() {
        mVideoView.start();
    }
    @Override
    public void pause() {
        mVideoView.pause();
    }
}
