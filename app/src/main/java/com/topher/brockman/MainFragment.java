package com.topher.brockman;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.*;
import com.google.gson.Gson;
import com.topher.brockman.api.API;
import com.topher.brockman.api.Broadcast;
import com.topher.brockman.api.TSchau;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 16/07/16.
 */

public class MainFragment extends BrowseFragment
        implements OnItemViewClickedListener {

    public static final String API_URL = "http://www.tagesschau.de/api/";
    public static final int NUM_OF_TASCHAUS = 12;
    public static final String EXTRA_VIDEO = "extra_video";

    private List<TSchau> tschauList = new ArrayList<TSchau>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
        setHeadersState(HEADERS_ENABLED);
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
        CardPresenter presenter = new CardPresenter();
        ArrayObjectAdapter listRowAdapter =
                new ArrayObjectAdapter( presenter );

        for (TSchau t : tschauList) {
            listRowAdapter.add(t);
        }

        HeaderItem header = new HeaderItem(adapter.size() - 1, "tagesschau");
        adapter.add(new ListRow(header, listRowAdapter));

        setAdapter(adapter);
    }

    private void loadData() {
/*        String json = Utils.loadJSONFromUrl(
                "http://www.tagesschau.de/api/");
        Type collection = new TypeToken<ArrayList<API>>(){}.getType();
        Gson gson = new Gson();
        mAPIs = gson.fromJson(json, collection);
        System.out.println(mAPIs.get(0).type + " " + mAPIs.get(0).details);*/
        new DownloadFilesTask().execute();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            loadRows();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected Void doInBackground(Void... params) {
            String latest = Utils.extractLatestTSchau(API_URL);
            List<String> tschauUrls = new ArrayList<String>();
            tschauUrls.add(latest);

            String id_string = latest.replaceAll("[^0-9]+","");
            int id = Integer.parseInt(id_string);
            for (int i = 1; i < NUM_OF_TASCHAUS - 1; i++) {
                String tschau = latest.replaceAll("[0-9]+",
                        Integer.toString(id - 2*i));
                tschauUrls.add(tschau);
            }

            for (String url : tschauUrls) {
                String json = Utils.loadJSONFromUrl(url);
                Gson gson = new Gson();
                Broadcast bc = null;
                try {
                    bc = gson.fromJson(json, Broadcast.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (bc != null) {
                    TSchau t = new TSchau();
                    t.setDate(bc.broadcastDate);
                    t.setImgUrl(bc.getImageUrl());
                    t.setVideoUrl(bc.getVideoUrl());
                    t.setLength(Utils.convertLengthString(bc.getVideoDuration()));
                    tschauList.add(t);
                }

            }

            return null;
        }
    }


}
