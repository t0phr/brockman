package com.topher.brockman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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

    private List<Tagesschau> tschauList = new ArrayList<Tagesschau>();
    private List<Tagesthemen> tthemenList = new ArrayList<Tagesthemen>();
    private List<TsVor20Jahren> tsVor20JahrenList = new ArrayList<TsVor20Jahren>();

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
        if (item instanceof Broadcast) {
            Broadcast video = (Broadcast) item;
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
        for (Tagesschau t : tschauList) {
            ts_adapter.add(t);
        }

        HeaderItem ts_header = new HeaderItem(adapter.size() - 1, "Tagesschau");
        adapter.add(new ListRow(ts_header, ts_adapter));


        ArrayObjectAdapter tt_adapter =
                new ArrayObjectAdapter( presenter );
        for (Tagesthemen tt : tthemenList) {
            tt_adapter.add(tt);
        }

        HeaderItem tt_header = new HeaderItem(adapter.size() - 1, "Tagesthemen");
        adapter.add(new ListRow(tt_header, tt_adapter));


        ArrayObjectAdapter t20_adapter =
                new ArrayObjectAdapter( presenter );
        for (TsVor20Jahren t20 : tsVor20JahrenList) {
            t20_adapter.add(t20);
        }

        HeaderItem t20_header = new HeaderItem(adapter.size() - 1, "Tagesschau vor 20 Jahren");
        adapter.add(new ListRow(t20_header, t20_adapter));

        setAdapter(adapter);
    }

    private void loadData() {
        new DownloadFilesTask().execute();
    }

    private BroadcastDetails retrieveAndParse(String url) throws IOException {
        BroadcastDetails bcd;
        Gson gson = new Gson();

        try {
            String json = Utils.loadJSONFromUrl(url);
            bcd = gson.fromJson(json, BroadcastDetails.class);
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Answer from server for url "
                    + url
                    + " is not in JSON Syntax. Retrying next url...");
            throw e;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File at url "
                    + url
                    + " not found. Retrying next url...");
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Unknown error occurred while trying to access "
                    + url
                    + " Retrying next url...");
            throw e;
        }

        if (bcd != null) return bcd;
        else throw new IOException();

    }

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

    private class TaskStatus {
        public boolean success = true;

        public TaskStatus fail() {
            success = false;
            return this;
        }
    }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, TaskStatus> {
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
            String latestTS, latestTT, latestTSV20;

            try {
                latestTS = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tagesschau_base_url));
                latestTT = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tagesthemen_base_url));
                latestTSV20 = Utils.extractLatestBroadcast(getResources()
                        .getString(R.string.api_tsvor20_base_url));
            } catch (IOException e) {
                Log.e(TAG, "Network error. Aborting...");
                return new TaskStatus().fail();
            }


            String ts_id_string = latestTS.replaceAll(ID_REGEX_EXTRACT,"");
            int ts_id = Integer.parseInt(ts_id_string);

            String tt_id_string = latestTT.replaceAll(ID_REGEX_EXTRACT, "");
            int tt_id = Integer.parseInt(tt_id_string);

            String tsv20_id_string = latestTSV20.replaceAll(ID_REGEX_EXTRACT, "");
            int tsv20_id = Integer.parseInt(tsv20_id_string);


            BroadcastDetails bcd = null;

            int retrieved = 0;
            for (int i = 0; i < NUM_MAX_TRIES; i++) {
                String url = latestTS.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(ts_id - 2*i));

                try { bcd = retrieveAndParse(url); }
                catch (Exception e) { continue; }

                Tagesschau t = new Tagesschau();
                t.setDate(bcd.broadcastDate);
                t.setImgUrl(bcd.getImageUrl());
                t.setVideoUrl(bcd.getVideoUrl());
                t.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                t.setTitle(bcd.broadcastTitle);
                tschauList.add(t);
                retrieved++;

                if (retrieved == NUM_OF_TS) {
                    break;
                }
            }

            retrieved = 0;
            for (int i = 0; i < NUM_MAX_TRIES; i++) {
                String url = latestTT.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(tt_id - 2*i));

                try { bcd = retrieveAndParse(url); }
                catch (Exception e) { continue; }

                Tagesthemen tt = new Tagesthemen();
                tt.setDate(bcd.broadcastDate);
                tt.setImgUrl(bcd.getImageUrl());
                tt.setVideoUrl(bcd.getVideoUrl());
                tt.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                tt.setTitle(bcd.broadcastTitle);
                tthemenList.add(tt);
                retrieved++;

                if (retrieved == NUM_OF_TT) {
                    break;
                }
            }

            retrieved = 0;
            for (int i = 0; i < NUM_MAX_TRIES; i++) {
                String url = latestTSV20.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(tsv20_id - 2*i));

                try { bcd = retrieveAndParse(url); }
                catch (Exception e) { continue; }

                TsVor20Jahren t20 = new TsVor20Jahren();
                t20.setDate(bcd.broadcastDate);
                t20.setImgUrl(bcd.getImageUrl());
                t20.setVideoUrl(bcd.getTSV20VideoUrl());
                t20.setLength(Utils.convertLengthString(bcd.getTSV20Duration()));
                t20.setTitle(bcd.broadcastTitle);
                tsVor20JahrenList.add(t20);
                retrieved++;

                if (retrieved == NUM_OF_TSV20) {
                    break;
                }
            }
            return new TaskStatus();
        }
    }
}
