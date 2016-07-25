package com.topher.brockman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by topher on 16/07/16.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
        startService(new Intent(this, RecommendationService.class));
    }
}
