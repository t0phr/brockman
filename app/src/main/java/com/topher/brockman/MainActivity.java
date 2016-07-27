package com.topher.brockman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * Created by topher on 16/07/16.
 */
public class MainActivity extends Activity {
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        startService(new Intent(this, RecommendationService.class));
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }
}
