package com.topher.brockman;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.*;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.Gson;
import com.google.gson.internal.Streams;
import com.topher.brockman.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 16/07/16.
 */

public class MainFragment extends BrowseFragment
        implements OnItemViewClickedListener {

    // public static final String API_URL = "http://www.tagesschau.de/api/";
    public static final String API_TS_URL =
            "http://tagesschau.de/api/multimedia/sendung/letztesendungen100~_type-TS.json";
    public static final String API_TT_URL =
            "http://tagesschau.de/api/multimedia/sendung/letztesendungen100~_type-TT.json";
    public static final String API_TSV20_URL =
            "http://tagesschau.de/api/multimedia/sendung/letztesendungen100~_type-TSV20.json";
    public static final String API_TS20_URL =
            "http://tagesschau.de/api/multimedia/sendung/letztesendungen100~_type-TS2000.json";
    public static final String ID_REGEX_EXTRACT = "[^0-9]+";
    public static final String ID_REGEX_REPLACE = "[0-9]+";
    public static final int NUM_OF_TS = 5;
    public static final int NUM_OF_TSV20 = 1;
    public static final int NUM_OF_TT = 3;
    public static final int NUM_MAX_TRIES = 12;
    public static final String EXTRA_VIDEO = "extra_video";

    private List<TSchau> tschauList = new ArrayList<TSchau>();
    private List<TThemen> tthemenList = new ArrayList<TThemen>();
    private List<TSV20> tsv20List = new ArrayList<TSV20>();

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
        if (item instanceof TSchau) {
            TSchau video = (TSchau) item;
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

        for (TSchau t : tschauList) {
            ts_adapter.add(t);
        }

        HeaderItem ts_header = new HeaderItem(adapter.size() - 1, "Tagesschau");
        adapter.add(new ListRow(ts_header, ts_adapter));


        ArrayObjectAdapter tt_adapter =
                new ArrayObjectAdapter( presenter );
        for (TThemen tt : tthemenList) {
            tt_adapter.add(tt);
        }

        HeaderItem tt_header = new HeaderItem(adapter.size() - 1, "Tagesthemen");
        adapter.add(new ListRow(tt_header, tt_adapter));


        ArrayObjectAdapter t20_adapter =
                new ArrayObjectAdapter( presenter );
        for (TSV20 t20 : tsv20List) {
            t20_adapter.add(t20);
        }

        HeaderItem t20_header = new HeaderItem(adapter.size() - 1, "Tagesschau vor 20 Jahren");
        adapter.add(new ListRow(t20_header, t20_adapter));

        setAdapter(adapter);
    }

    private void loadData() {
        new DownloadFilesTask().execute();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            loadRows();
            ((MainActivity) getActivity()).getProgressBar().setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected Void doInBackground(Void... params) {
            String latestTS = Utils.extractLatestBroadcast(API_TS_URL);
            String latestTT = Utils.extractLatestBroadcast(API_TT_URL);
            String latestTSV20 = Utils.extractLatestBroadcast(API_TSV20_URL);

            List<String> tschauUrls = new ArrayList<String>();
            List<String> tthemenUrls = new ArrayList<>();
            List<String> tsv20Urls = new ArrayList<>();

            tschauUrls.add(latestTS);
            tthemenUrls.add(latestTT);
            tsv20Urls.add(latestTSV20);

            String ts_id_string = latestTS.replaceAll(ID_REGEX_EXTRACT,"");
            int ts_id = Integer.parseInt(ts_id_string);

            String tt_id_string = latestTT.replaceAll(ID_REGEX_EXTRACT, "");
            int tt_id = Integer.parseInt(tt_id_string);

            String tsv20_id_string = latestTT.replaceAll(ID_REGEX_EXTRACT, "");
            int tsv20_id = Integer.parseInt(tsv20_id_string);

            for (int i = 1; i < NUM_OF_TS; i++) {
                String tschau = latestTS.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(ts_id - 2*i));
                tschauUrls.add(tschau);
            }

            for (int i = 1; i < NUM_OF_TT; i++) {
                String tt = latestTT.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(tt_id - 2*i));
                tthemenUrls.add(tt);
            }

            for (int i = 1; i < NUM_OF_TSV20; i++) {
                String tsv20 = latestTSV20.replaceAll(ID_REGEX_REPLACE,
                        Integer.toString(tsv20_id - 2*i));
                tsv20Urls.add(tsv20);
            }

            Gson gson = new Gson();
            BroadcastDetails bcd = null;

            for (String url : tschauUrls) {
                bcd = null;
                String json = Utils.loadJSONFromUrl(url);

                try {
                    bcd = gson.fromJson(json, BroadcastDetails.class);
                } catch (Exception e) { // TODO: proper exception handling
                    e.printStackTrace();
                }

                if (bcd != null) {
                    TSchau t = new TSchau();
                    t.setDate(bcd.broadcastDate);
                    t.setImgUrl(bcd.getImageUrl());
                    t.setVideoUrl(bcd.getVideoUrl());
                    t.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                    t.setTitle(bcd.broadcastTitle);
                    tschauList.add(t);
                }
            }

            for (String url : tthemenUrls) {
                bcd = null;
                String json = Utils.loadJSONFromUrl(url);

                try {
                    bcd = gson.fromJson(json, BroadcastDetails.class);
                } catch (Exception e) { // TODO: proper exception handling
                    e.printStackTrace();
                }

                if (bcd != null) {
                    TThemen tt = new TThemen();
                    tt.setDate(bcd.broadcastDate);
                    tt.setImgUrl(bcd.getImageUrl());
                    tt.setVideoUrl(bcd.getVideoUrl());
                    tt.setLength(Utils.convertLengthString(bcd.getVideoDuration()));
                    tt.setTitle(bcd.broadcastTitle);
                    tthemenList.add(tt);
                }
            }

            for (String url : tsv20Urls) {
                bcd = null;
                String json = Utils.loadJSONFromUrl(url);

                try {
                    bcd = gson.fromJson(json, BroadcastDetails.class);
                } catch (Exception e) { // TODO: proper exception handling
                    e.printStackTrace();
                }

                if (bcd != null) {
                    TSV20 t20 = new TSV20();
                    t20.setDate(bcd.broadcastDate);
                    t20.setImgUrl(bcd.getImageUrl());
                    t20.setVideoUrl(bcd.getTSV20VideoUrl());
                    t20.setLength(Utils.convertLengthString(bcd.getTSV20Duration()));
                    t20.setTitle(bcd.broadcastTitle);
                    tsv20List.add(t20);
                }
            }

            return null;
        }
    }


}
