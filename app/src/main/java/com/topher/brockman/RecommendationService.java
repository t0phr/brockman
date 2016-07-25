package com.topher.brockman;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.topher.brockman.api.BroadcastDetails;
import com.topher.brockman.api.TSV20;
import com.topher.brockman.api.TSchau;
import com.topher.brockman.api.TThemen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 25/07/16.
 */
public class RecommendationService extends IntentService {
    public static final String TAG = "RecommendationService";
    private TSchau ts;

    public RecommendationService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        loadData();
        Log.d(TAG, "Updating recommendation cards");
    }

    private void buildNotification(Bitmap bitmap) {
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.BigPictureStyle(
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.notification)
                        .setLocalOnly(true)
                        .setOngoing(true)
                        .setColor(ContextCompat.getColor(getApplicationContext(),
                                android.R.color.black))
                        .setCategory(Notification.CATEGORY_RECOMMENDATION)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentTitle(ts.getTitle())
                        .setContentText(Utils.getContentDescription(ts))
                        .setLargeIcon(bitmap)
                        .setContentIntent(buildPendingIntent(ts, 1)))
                .build();

        notificationManager.notify(0, notification);
    }

    private PendingIntent buildPendingIntent(TSchau video, long id ) {
        Intent playIntent = new Intent(this, PlayerActivity.class);
        playIntent.putExtra(MainFragment.EXTRA_VIDEO, video);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(PlayerActivity.class);
        stackBuilder.addNextIntent(playIntent);
        playIntent.setAction(Long.toString(id));

        PendingIntent intent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }

    private void loadData() {
        new RecommendationService.DownloadFilesTask().execute();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, Void> {
        Bitmap bitmap;

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected Void doInBackground(Void... params) {
            String latestTS = Utils.extractLatestBroadcast(MainFragment.API_TS20_URL);

            Gson gson = new Gson();
            BroadcastDetails bcd = null;

            String json = Utils.loadJSONFromUrl(latestTS);

            try {
                bcd = gson.fromJson(json, BroadcastDetails.class);
            } catch (Exception e) { // TODO: proper exception handling
                e.printStackTrace();
            }

            if (bcd != null) {
                ts = new TSchau();
                ts.setDate(bcd.broadcastDate);
                ts.setImgUrl(bcd.getImageUrl());
                ts.setVideoUrl(bcd.getVideoUrl());
                ts.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                ts.setTitle(bcd.broadcastTitle);
            }


            try {
                bitmap = Picasso.with(getApplicationContext())
                        .load(ts.getImgUrl())
                        .resize(313, 176)
                        .get();
            } catch( IOException e ) { // TODO: proper exception handling
                e.printStackTrace();
            }

            buildNotification(bitmap);

            return null;
        }
    }
}
