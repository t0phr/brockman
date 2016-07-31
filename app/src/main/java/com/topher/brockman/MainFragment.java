package com.topher.brockman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.*;
import android.util.Log;
import android.view.View;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.topher.brockman.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 16/07/16.
 */

public class MainFragment extends BrowseFragment
        implements OnItemViewClickedListener {
    private static final String TAG = "MainFragment";
    private static final boolean DEBUG = false;

    private static final String ID_REGEX_EXTRACT = "[^0-9]+";
    private static final String ID_REGEX_REPLACE = "[0-9]+";

    private static final int NUM_OF_TS = 7;
    private static final int NUM_OF_TSV20 = 1;
    private static final int NUM_OF_TT = 3;
    private static final int NUM_MAX_TRIES = 12;

    public static final String EXTRA_VIDEO = "extra_video";

    private List<Tagesschau> tsList = new ArrayList<Tagesschau>();
    private List<Tagesthemen> ttList = new ArrayList<Tagesthemen>();
    private List<TsVor20Jahren> t20List = new ArrayList<TsVor20Jahren>();
    private Tagesschau24 t24;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSearchAffordanceColor(R.color.card_info_bg_color);
        loadData();
        setBadgeDrawable(getContext().getDrawable(R.drawable.logo));
        setHeadersState(HEADERS_DISABLED);
        setOnItemViewClickedListener(this);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder,
                              Object item,
                              RowPresenter.ViewHolder rowViewHolder,
                              Row row) {
        if (item instanceof Playable) {
            Playable video = (Playable) item;
            Intent intent = new Intent(getActivity(),
                    PlayerActivity.class );
            intent.putExtra(MainFragment.EXTRA_VIDEO, video);
            startActivity(intent);
        }
    }

    private void loadRows() {
        ArrayObjectAdapter adapter =
                new ArrayObjectAdapter( new ListRowPresenter() );
        CardPresenter presenter = new CardPresenter(getContext());


        ArrayObjectAdapter ts_adapter =
                new ArrayObjectAdapter( presenter );
        if (t24 != null)
            ts_adapter.add(t24);

        for (Tagesschau t : tsList) {
            ts_adapter.add(t);
        }

        HeaderItem ts_header = new HeaderItem(adapter.size() - 1, "Tagesschau");
        adapter.add(new ListRow(ts_header, ts_adapter));


        ArrayObjectAdapter tt_adapter =
                new ArrayObjectAdapter( presenter );
        for (Tagesthemen tt : ttList) {
            tt_adapter.add(tt);
        }

        HeaderItem tt_header = new HeaderItem(adapter.size() - 1, "Tagesthemen");
        adapter.add(new ListRow(tt_header, tt_adapter));


        ArrayObjectAdapter t20_adapter =
                new ArrayObjectAdapter( presenter );
        for (TsVor20Jahren t20 : t20List) {
            t20_adapter.add(t20);
        }

        HeaderItem t20_header = new HeaderItem(adapter.size() - 1, "Tagesschau vor 20 Jahren");
        adapter.add(new ListRow(t20_header, t20_adapter));

        setAdapter(adapter);
    }

    private void loadData() {
        new DownloadFilesTask().execute();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, TaskStatus> {

        private void showNetworkErrorDialog() {
            new AlertDialog.Builder(getContext())
                    .setTitle("Server not responding")
                    .setMessage("Do you want to quit or retry?")
                    .setPositiveButton(R.string.error_dialog_answer_quit,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finishAffinity();
                                }
                            })
                    .setNegativeButton(R.string.error_dialog_answer_retry,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MainActivity) getActivity()).getProgressBar().setVisibility(View.VISIBLE);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadData();
                                        }
                                    }, 5000);
                                }
                            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        private <T extends Broadcast>
        void retrieveBroadcasts(String baseUrl, int broadcstId, List<T> list,
                                int count, Class<T> c) {
            BroadcastDetails bcd = null;

            int retrieved = 0;
            for (int i = 0; i < NUM_MAX_TRIES; i++) {
                String url = baseUrl.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(broadcstId - 2*i));

                try { bcd = Utils.parseJsonToClass(url,
                        BroadcastDetails.class); }
                catch (Exception e) {
                    if (i < NUM_MAX_TRIES - 1)
                        Log.w(TAG, "Retrying next url.");
                    continue;
                }

                T t;
                try { t = c.newInstance(); }
                catch (Exception e) {
                    Log.e(TAG, "retrieveBroadcasts() called with " +
                            "incompatible class type.");
                    return;
                }

                t.setDate(bcd.broadcastDate);
                t.setImgUrl(bcd.getImageUrl());
                t.setVideoUrl(bcd.getVideoUrl());
                t.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                t.setTitle(bcd.broadcastTitle);
                list.add(t);
                retrieved++;

                if (retrieved == count) {
                    break;
                }
            }
        }

        @Override
        protected void onPostExecute(TaskStatus status) {
            ((MainActivity) getActivity()).getProgressBar().setVisibility(View.GONE);

            if (status.success) {
                loadRows();
            } else {
                showNetworkErrorDialog();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected TaskStatus doInBackground(Void... params) {
            String latestTS, latestTT, latestT20;

            try {
                latestTS = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tagesschau_base_url));
                latestTT = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tagesthemen_base_url));
                latestT20 = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tsvor20_base_url));
            } catch (IOException e) {
                Log.e(TAG, "Network error. Aborting...");
                return new TaskStatus().fail();
            }


            String ts_id_string = latestTS.replaceAll(ID_REGEX_EXTRACT,"");
            int ts_id = Integer.parseInt(ts_id_string);

            String tt_id_string = latestTT.replaceAll(ID_REGEX_EXTRACT, "");
            int tt_id = Integer.parseInt(tt_id_string);

            String tsv20_id_string = latestT20.replaceAll(ID_REGEX_EXTRACT, "");
            int tsv20_id = Integer.parseInt(tsv20_id_string);


            retrieveBroadcasts(latestTS, ts_id, tsList, NUM_OF_TS,
                    Tagesschau.class);

            retrieveBroadcasts(latestTT, tt_id, ttList, NUM_OF_TT,
                    Tagesthemen.class);

            retrieveBroadcasts(latestT20, tsv20_id, t20List,
                    NUM_OF_TSV20, TsVor20Jahren.class);

            try { t24 = Utils.parseJsonToClass(
                    getResources().getString(R.string.api_base_url),
                    API.class).getTagesschau24();
                t24.setImgUrl(getResources().getString(R.string.api_t24_image_url));
            } catch (IOException e) {
                Log.e(TAG, "Error retrieving tagesschau24 livestream, skipping.");
            }

            return new TaskStatus();
        }
    }
}
