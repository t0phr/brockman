package com.topher.brockman;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.VideoView;
import com.topher.brockman.api.Broadcast;

/**
 * Created by topher on 21/07/16.
 */
public class PlayerActivity extends Activity {
    private VideoView mVideoView;
    private ProgressBar mProgressBar;
    private Broadcast mVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mProgressBar = (ProgressBar) findViewById(R.id.video_progress_bar);
        mVideo = (Broadcast) getIntent().getSerializableExtra(MainFragment.
                EXTRA_VIDEO);
    }

    public VideoView getVideoView() {
        return mVideoView;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }
}
